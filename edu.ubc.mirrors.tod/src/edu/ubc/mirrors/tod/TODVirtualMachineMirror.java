/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package edu.ubc.mirrors.tod;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.objectweb.asm.Type;

import tod.core.config.TODConfig;
import tod.core.database.browser.ICompoundInspector.EntryValue;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.core.session.ISession;
import tod.core.session.SessionTypeManager;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.EventDispatch;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.StaticFieldValuesMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.raw.ArrayClassMirror;

public class TODVirtualMachineMirror implements VirtualMachineMirror {

    final ILogBrowser logBrowser;
    final TODMirrorEventRequestManager requestManager;
    final TODMirrorEventQueue queue = new TODMirrorEventQueue(this);
    private final EventDispatch dispatch = new EventDispatch(this);
    
    // Some special case thread awkwardness handling
    private final ClassMirror threadClass;
    final Map<ObjectId, IThreadInfo> threadInfosByObjectId = new HashMap<ObjectId, IThreadInfo>();
    
    private final Map<Object, ObjectMirror> mirrors = new HashMap<Object, ObjectMirror>();
    
    // Need to link between exit events and their call events to find the target objects
    // of constructor exit events, since they don't store their return value correctly.
    private final Map<IBehaviorExitEvent, IBehaviorCallEvent> callEventsByExitEvents = 
            new HashMap<IBehaviorExitEvent, IBehaviorCallEvent>();
    
    public TODVirtualMachineMirror(ILogBrowser logBrowser) {
        super();
        this.logBrowser = logBrowser;
        this.requestManager = new TODMirrorEventRequestManager(this);
        
        for (IThreadInfo threadInfo : logBrowser.getThreads()) {
            threadInfosByObjectId.put(threadInfo.getObjectId(), threadInfo);
        }
        
        threadClass = findBootstrapClassMirror(Thread.class.getName());

        // Precalculate the thread mirrors so we can store them by IThreadInfo
        for (ObjectMirror thread : threadClass.getInstances()) {
            IThreadInfo threadInfo = ((TODThreadMirror)thread).threadInfo;
            mirrors.put(threadInfo, thread);
        }
    }
    
    public Iterable<ILogEvent> asIterable(final IEventBrowser browser) {
        return new Iterable<ILogEvent>() {
            @Override
            public Iterator<ILogEvent> iterator() {
                return new Iterator<ILogEvent>() {
                    @Override
                    public boolean hasNext() {
                        return browser.hasNext();
                    }

                    @Override
                    public ILogEvent next() {
                        return browser.next();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                    
                };
            }
        };
    }
    
    public Iterable<ILogEvent> allEvents(IEventFilter filter) {
        final IEventBrowser browser = logBrowser.createBrowser(filter);
        return asIterable(browser);
    }
    
    public Iterable<ILogEvent> allEvents() {
        final IEventBrowser browser = logBrowser.createBrowser();
        return asIterable(browser);
    }
    
    public static TODVirtualMachineMirror connect(String clientName) {
        URI theUri = URI.create("tod-dbgrid-remote:/");
        TODConfig theConfig = new TODConfig();
        theConfig.set(TODConfig.CLIENT_NAME, clientName);
        ISession theSession = SessionTypeManager.getInstance().createSession(null, theUri, theConfig);
        ILogBrowser logBrowser = theSession.getLogBrowser();

        return new TODVirtualMachineMirror(logBrowser);
    }
    
    public ILogBrowser getLogBrowser() {
        return logBrowser;
    }
    
    public long currentTimestamp() {
        return requestManager.currentTimestamp();
    }
    
    public ObjectMirror makeMirror(Object todObject) {
        if (todObject == null) {
            return null;
        }

        ObjectMirror result = mirrors.get(todObject);
        if (result != null) {
            return result;
        }

        if (todObject instanceof ITypeInfo) {
            ITypeInfo typeInfo = (ITypeInfo)todObject;
            // Ignore class infos with no bytecode - they are essentially not actually loaded classes.
            if (typeInfo instanceof IClassInfo && !typeInfo.isPrimitive() && ((IClassInfo)todObject).getBytecode() == null) {
                result = null;
            } else {
                result = new TODClassMirror(this, typeInfo);
            }
        } else if (todObject instanceof ObjectId) {
            IObjectInspector inspector = logBrowser.createObjectInspector((ObjectId)todObject);
            result = makeMirror(inspector);
        } else if (todObject instanceof IObjectInspector) {
            IObjectInspector inspector = (IObjectInspector)todObject;
            ClassMirror type = makeClassMirror(inspector.getType());
            if (type.isArray()) {
                result = new TODArrayMirror(this, inspector);
            } else if (Reflection.isAssignableFrom(threadClass, type)) {
                result = new TODThreadMirror(this, inspector);
            } else if (type.getClassName().equals(Class.class.getName())) {
                // Read the class name out of the instance variable
                TODInstanceMirror asInstance = new TODInstanceMirror(this, inspector);
                InstanceMirror nameMirror;
                try {
                    nameMirror = (InstanceMirror)asInstance.get(type.getDeclaredField("name"));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                String name = Reflection.getRealStringForMirror(nameMirror);
                Type asmType = Reflection.typeForClassName(name);
                if (asmType.getSort() == Type.OBJECT || asmType.getSort() == Type.ARRAY) {
                    // TODO-RS: Again, TOD doesn't support multiple class loaders properly.
                    result = findBootstrapClassMirror(name);
                } else {
                    result = getPrimitiveClass(name);
                }
            } else {
                result = new TODInstanceMirror(this, inspector);
            }
        } else if (todObject instanceof IThreadInfo) {
            result = new TODThreadMirror(this, (IThreadInfo)todObject);
        } else {
            throw new IllegalArgumentException("Unrecognized object type: " + todObject.getClass());
        }
        
        mirrors.put(todObject, result);
        return result;
    }
    
    public TODClassMirror makeClassMirror(ITypeInfo type) {
        return (TODClassMirror)makeMirror(type);
    }
    
    public List<ClassMirror> makeClassMirrorList(IClassInfo[] classInfos) {
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        for (IClassInfo classInfo : classInfos) {
            result.add(makeClassMirror(classInfo));
        }
        return result;
    }
    
    public ThreadMirror makeThreadMirror(IThreadInfo threadInfo) {
        return (ThreadMirror)makeMirror(threadInfo);
    }
    
    @Override
    public TODClassMirror findBootstrapClassMirror(String name) {
        // TODO-RS: See TODClassMirror#getLoader()
        return makeClassMirror(logBrowser.getStructureDatabase().getClass(name, false));
    }

    @Override
    public ClassMirror defineBootstrapClass(String name, ByteArrayMirror b, int off, int len) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration<URL> findBootstrapResources(String path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ClassMirror> findAllClasses(String name, boolean includeSubclasses) {
        // TODO-RS: IClassInfos have a start time, so we should be filtering out those
        // classes that haven't been defined yet.
        // See also TODClassMirrorPrepareRequest.
        // TODO-RS: calling getClasses(name) would be better but that's not actually implemented :)
        IClassInfo match = logBrowser.getStructureDatabase().getClass(name, false);
        if (match == null) {
            return Collections.emptyList();
        }
        ClassMirror klass = makeClassMirror(match);
        
        if (includeSubclasses) {
            return Reflection.collectAllSubclasses(klass);
        } else {
            return Collections.singletonList(klass);
        }
    }

    @Override
    public List<ThreadMirror> getThreads() {
        List<ThreadMirror> result = new ArrayList<ThreadMirror>();
        for (IThreadInfo threadInfo : logBrowser.getThreads()) {
            result.add(makeThreadMirror(threadInfo));
        }
        return result;
    }

    @Override
    public ClassMirror getPrimitiveClass(String name) {
        Type primitiveType = Reflection.typeForClassName(name);
        return makeClassMirror(logBrowser.getStructureDatabase().getType(primitiveType.getDescriptor(), false));
    }

    @Override
    public ClassMirror getArrayClass(int dimensions, ClassMirror elementClass) {
        String name = elementClass.getClassName();
        for (int d = 0; d < dimensions; d++) {
            name += "[]";
        }
        ClassMirror result = findBootstrapClassMirror(name);
        if (result != null) {
            return result;
        }
        
        return new ArrayClassMirror(dimensions, elementClass);
    }

    @Override
    public TODMirrorEventRequestManager eventRequestManager() {
        return requestManager;
    }

    @Override
    public MirrorEventQueue eventQueue() {
        return queue;
    }

    @Override
    public void suspend() {
        // No-op: the trace is always "suspended"
    }

    @Override
    public void resume() {
        requestManager.resume();
    }

    @Override
    public boolean canBeModified() {
        return false;
    }

    @Override
    public EventDispatch dispatch() {
        return dispatch;
    }

    @Override
    public void addCallback(MirrorEventRequest request, Callback<MirrorEvent> callback) {
        dispatch.addCallback(request, callback);
    }
    
    @Override
    public List<ClassMirror> findAllClasses() {
        // TODO-RS: This should be all classes at a particular timestamp,
        // not all classes ever!
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        for (IClassInfo classInfo : logBrowser.getStructureDatabase().getClasses()) {
            ClassMirror classMirror = makeClassMirror(classInfo);
            if (classMirror != null) {
                result.add(classMirror);
            }
        }
        return result;
    }

    @Override
    public boolean canGetBytecodes() {
        return true;
    }

    @Override
    public boolean hasClassInitialization() {
        return true;
    }

    @Override
    public InstanceMirror makeString(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InstanceMirror getInternedString(String s) {
        throw new UnsupportedOperationException();
    }

    public Object wrapEntryValues(ClassMirror type, EntryValue[] values) {
        if (values.length != 1) {
            throw new IllegalStateException("Missing/ambiguous values: " + Arrays.toString(values));
        }
        return wrapValue(type, values[0].getValue());
    }
    
    public Object wrapValue(ClassMirror type, Object value) {
        if (value instanceof ObjectId) {
            return makeMirror(value);
        } else if (type.getClassName().equals("long")) {
            return ((Number)value).longValue();
        } else {
            return value;
        }
    }

    public FrameMirror makeFrameMirror(ILogEvent event) {
        if (event instanceof IBehaviorCallEvent) {
            return new TODBehaviorCallFrameMirror(this, (IBehaviorCallEvent)event);
        } else {
            throw new IllegalArgumentException("Unsupported event type: " + event.getClass());
        }
    }

    public MethodMirror makeMethodMirror(IBehaviorInfo behavior) {
        return new TODMethodOrConstructorMirror(this, behavior);
    }
    
    public ConstructorMirror makeConstructorMirror(IBehaviorInfo behavior) {
        return new TODMethodOrConstructorMirror(this, behavior);
    }
    
    @Override
    public void gc() {
        // TODO-RS: Only traversing from static variables for now,
        // since TOD doesn't track frame local variables.
        List<ClassMirror> loadedClasses = findAllClasses();
        Set<ObjectMirror> reachable = reachable(loadedClasses);
        for (ObjectMirror mirror : mirrors.values()) {
            if (!reachable.contains(mirror) && mirror instanceof TODInstanceMirror) {
                ((TODInstanceMirror)mirror).collectable();
            }
        }
    }
    
    private static Set<ObjectMirror> reachable(Collection<? extends ObjectMirror> roots) {
        // Simple inefficient implementation of mark-and-sweep garbage collection
        Set<ObjectMirror> visited = new HashSet<ObjectMirror>();
        Stack<ObjectMirror> toVisit = new Stack<ObjectMirror>();
        toVisit.addAll(roots);
        while (!toVisit.isEmpty()) {
            ObjectMirror visiting = toVisit.pop();
            visited.add(visiting);
            
            if (visiting instanceof ClassMirror) {
                ClassMirror classMirror = (ClassMirror)visiting;
                StaticFieldValuesMirror staticValues = classMirror.getStaticFieldValues();
                for (FieldMirror field : Reflection.getAllFields(classMirror)) {
                    TODFieldMirror todField = (TODFieldMirror)field;
                    if (field.getType() != null && !field.getType().isPrimitive() && todField.field.isStatic()) {
                        ObjectMirror child;
                        try {
                            child = staticValues.get(field);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                        if (!visited.contains(child)) {
                            toVisit.add(child);
                        }
                    }
                }
            } else if (visiting instanceof InstanceMirror) {
                InstanceMirror instanceMirror = (InstanceMirror)visiting;
                for (FieldMirror field : Reflection.getAllFields(instanceMirror.getClassMirror())) {
                    TODFieldMirror todField = (TODFieldMirror)field;
                    if (field.getType() != null && !field.getType().isPrimitive() && !todField.field.isStatic()) {
                        ObjectMirror child;
                        try {
                            child = instanceMirror.get(field);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                        if (!visited.contains(child)) {
                            toVisit.add(child);
                        }
                    }
                }
            } else if (visiting instanceof ObjectArrayMirror) {
                ObjectArrayMirror arrayMirror = (ObjectArrayMirror)visiting;
                ClassMirror classMirror = arrayMirror.getClassMirror();
                if (!classMirror.getComponentClassMirror().isPrimitive()) {
                    int length = arrayMirror.length();
                    for (int index = 0; index < length; index++) {
                        ObjectMirror child = arrayMirror.get(index);
                        if (!visited.contains(child)) {
                            toVisit.add(child);
                        }
                    }
                }
            }
        }
        
        return visited;
    }
    
    public void registerConstructorEntry(IBehaviorCallEvent entryEvent) {
        callEventsByExitEvents.put(entryEvent.getExitEvent(), entryEvent);
    }
    
    public IBehaviorCallEvent getEntryEvent(IBehaviorExitEvent exitEvent) {
        return callEventsByExitEvents.get(exitEvent);
    }
}
