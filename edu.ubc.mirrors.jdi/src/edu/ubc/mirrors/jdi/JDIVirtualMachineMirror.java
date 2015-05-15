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
package edu.ubc.mirrors.jdi;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Location;
import com.sun.jdi.LongValue;
import com.sun.jdi.Method;
import com.sun.jdi.Mirror;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VoidValue;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.AnnotationMirror.EnumMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.EventDispatch;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorLocation;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.raw.ArrayClassMirror;

public class JDIVirtualMachineMirror implements VirtualMachineMirror {

    protected final VirtualMachine jdiVM;
    private final EventDispatch dispatch;
    
    private final JDIMirrorEventRequestManager requestManager;
    private final MirrorEventQueue queue;
    
    private final Map<Mirror, ObjectMirror> mirrors = new HashMap<Mirror, ObjectMirror>();
    
    private final Map<String, ClassMirror> bootstrapClasses = new HashMap<String, ClassMirror>();
    
    public JDIVirtualMachineMirror(VirtualMachine jdiVM) {
        this.jdiVM = jdiVM;
        this.queue = //new JDIMirrorEventQueueBuffer(
                new JDIMirrorEventQueue(this, jdiVM.eventQueue());//);
        this.dispatch = new EventDispatch(this);
        this.requestManager = new JDIMirrorEventRequestManager(this, jdiVM.eventRequestManager());
        
//        ClassType threadType = (ClassType)jdiVM.classesByName(Thread.class.getName()).get(0);
//        Method constructor = threadType.methodsByName("<init>", "(Ljava/lang/String;)V").get(0);
//        try {
//            invokeThread = (ThreadReference)threadType.newInstance(pausedThread, constructor, Collections.singletonList(jdiVM.mirrorOf("invokeThread")), ClassType.INVOKE_SINGLE_THREADED);
//            
//            ThreadStartRequest request = jdiVM.eventRequestManager().createThreadStartRequest();
////            request.addThreadFilter(invokeThread);
////            request.addClassFilter("java.lang.Thread");
//            request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
//            request.enable();
//            
//            final Method startMethod = threadType.methodsByName("start").get(0);
//            
//            new Thread() {
//                public void run() {
//                    try {
//                        invokeThread.invokeMethod(pausedThread, startMethod, Collections.<Value>emptyList(), 0);
//                    } catch (InvalidTypeException e) {
//                        throw new RuntimeException(e);
//                    } catch (ClassNotLoadedException e) {
//                        throw new RuntimeException(e);
//                    } catch (IncompatibleThreadStateException e) {
//                        throw new RuntimeException(e);
//                    } catch (InvocationException e) {
//                        throw new RuntimeException(e);
//                    }
//                };
//            }.start();
//        } catch (InvalidTypeException e) {
//            throw new RuntimeException(e);
//        } catch (ClassNotLoadedException e) {
//            throw new RuntimeException(e);
//        } catch (IncompatibleThreadStateException e) {
//            throw new RuntimeException(e);
//        } catch (InvocationException e) {
//            throw new RuntimeException(e);
//        }
//        
//        try {
//            EventSet eventSet = jdiVM.eventQueue().remove();
//            eventSet.resume();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JDIVirtualMachineMirror)) {
            return false;
        }
        
        JDIVirtualMachineMirror other = (JDIVirtualMachineMirror)obj;
        return jdiVM == other.jdiVM;
    }
    
    @Override
    public int hashCode() {
        return 11 * jdiVM.hashCode();
    }
    
    @Override
    public ClassMirror findBootstrapClassMirror(String name) {
        ClassMirror result = bootstrapClasses.get(name);
        if (result != null) {
            return result;
        }
            
        for (ReferenceType t : jdiVM.classesByName(name)) {
            if (t.classLoader() == null) {
                result = makeClassMirror(t.classObject());
                bootstrapClasses.put(name, result);
                return result;
            }
        }
        
        return null;
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
    public List<ClassMirror> findAllClasses() {
        List<ClassMirror> classes = new ArrayList<ClassMirror>();
        for (ReferenceType t : jdiVM.allClasses()) {
            classes.add(((ClassMirror)makeMirror(t.classObject())));
        }
        return classes;
    }
    
    @Override
    public List<ClassMirror> findAllClasses(String name, boolean includeSubclasses) {
        List<ClassMirror> classes = new ArrayList<ClassMirror>();
        for (ReferenceType t : jdiVM.classesByName(name)) {
            if (includeSubclasses) {
        	collectSubclasses(t, classes);
            } else {
        	classes.add(((ClassMirror)makeMirror(t.classObject())));
            }
        }
        return classes;
    }

    private void collectSubclasses(ReferenceType t, List<ClassMirror> classes) {
	classes.add(((ClassMirror)makeMirror(t.classObject())));
	if (t instanceof ClassType) {
	    ClassType classType = (ClassType)t;
	    for (ReferenceType subclass : classType.subclasses()) {
		collectSubclasses(subclass, classes);
	    }
	} else if (t instanceof InterfaceType) {
	    InterfaceType interfaceType = (InterfaceType)t;
	    for (ReferenceType implementor : interfaceType.implementors()) {
		collectSubclasses(implementor, classes);
	    }
	    for (ReferenceType extender : interfaceType.subinterfaces()) {
		collectSubclasses(extender, classes);
	    }
	}
    }

    @Override
    public List<ThreadMirror> getThreads() {
        List<ThreadMirror> threads = new ArrayList<ThreadMirror>();
        for (ThreadReference t : jdiVM.allThreads()) {
            threads.add(((ThreadMirror)makeMirror(t)));
        }
        return threads;
    }

    public ObjectMirror makeMirror(ObjectReference t) {
        if (t == null) {
            return null;
        }
        
        ObjectMirror result = mirrors.get(t);
        if (result != null) {
            return result;
        }
        
        if (t instanceof ClassObjectReference) {
            result = new JDIClassMirror(this, (ClassObjectReference)t);
        } else if (t instanceof ClassLoaderReference) {
            result = new JDIClassLoaderMirror(this, (ClassLoaderReference)t);
        } else if (t instanceof ThreadReference) {
            result = new JDIThreadMirror(this, (ThreadReference)t);
        } else if (t instanceof ArrayReference) {
            result = new JDIArrayMirror(this, (ArrayReference)t);
        } else if (t instanceof ObjectReference) {
            result = new JDIInstanceMirror(this, (ObjectReference)t);
        } else {
            throw new IllegalArgumentException();
        }
        
        mirrors.put(t, result);
        
        return result;
    }

    public ClassMirror makeClassMirror(ObjectReference r) {
        return (ClassMirror)makeMirror(r);
    }
    
    public ClassMirror makeClassMirror(Type t) {
        if (t == null) {
            return null;
        } else if (t instanceof ReferenceType) {
            return (ClassMirror)makeMirror(((ReferenceType)t).classObject());
        } else {
            return getPrimitiveClass(t.name());
        }
    }
    
    public List<ClassMirror> makeClassMirrorList(List<? extends Type> types) {
        List<ClassMirror> result = new ArrayList<ClassMirror>(types.size());
        for (Type type : types) {
            result.add(makeClassMirror(type));
        }
        return result;
    }
    
    private final Map<String, ClassMirror> primitiveClasses = 
	  new HashMap<String, ClassMirror>();
    
    @Override
    public ClassMirror getPrimitiveClass(String name) {
	ClassMirror result = primitiveClasses.get(name);
	if (result != null) {
	    return result;
	}
	
	Class<?> boxingType = Reflection.getBoxingType(Reflection.typeForClassName(name));
	List<ReferenceType> classes = jdiVM.classesByName(boxingType.getName());
	if (classes.isEmpty()) {
	    return null;
	}
	
	ReferenceType boxingRT = classes.get(0);
	Field typeField = boxingRT.fieldByName("TYPE");
	ClassObjectReference cor = (ClassObjectReference)boxingRT.getValue(typeField);
	result = makeClassMirror(cor);
	primitiveClasses.put(name, result);
	
	return result;
	
	// Unfortunately we need to run code to get at these.
	// The method should be completely side-effect free though.
	// TODO-RS: This requires a thread, and it needs to be the "right" one or else we'll deadlock.
//	ReferenceType classType = jdiVM.classesByName(Class.class.getName()).get(0);
//	Method method = classType.methodsByName("getPrimitiveClass").get(0);
//	ThreadHolograph currentThread = ThreadHolograph.currentThreadMirror();
//	ThreadReference threadRef = ((JDIThreadMirror)currentThread.getWrapped()).thread;
//	ClassObjectReference cor;
//	try {
//	    cor = (ClassObjectReference)classType.classObject().invokeMethod(threadRef, method, Collections.singletonList(jdiVM.mirrorOf(name)), ObjectReference.INVOKE_SINGLE_THREADED);
//	} catch (InvalidTypeException e) {
//	    throw new InternalError(e.getMessage());
//	} catch (ClassNotLoadedException e) {
//	    // Should never happen - Class must be loaded by the time we get a VMStartEvent
//	    throw new RuntimeException(e);
//	} catch (IncompatibleThreadStateException e) {
//	    // Should never happen
//	    throw new RuntimeException(e);
//	} catch (InvocationException e) {
//	    throw new RuntimeException(e);
//	}
//	result = new JDIClassMirror(this, cor);
//	primitiveClasses.put(name, result);
//	return result;
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
    public JDIMirrorEventRequestManager eventRequestManager() {
	return requestManager;
    }
    
    @Override
    public MirrorEventQueue eventQueue() {
        return queue;
    }
    
    @Override
    public void suspend() {
	jdiVM.suspend(); 
    }
    
    @Override
    public void resume() {
	jdiVM.resume();
    }

    public Object wrapValue(Value value) {
	if (value == null || value instanceof VoidValue) {
	    return null;
	} else if (value instanceof BooleanValue) {
	    return ((BooleanValue)value).booleanValue();
	} else if (value instanceof ByteValue) {
	    return ((ByteValue)value).byteValue();
	} else if (value instanceof CharValue) {
	    return ((CharValue)value).charValue();
	} else if (value instanceof ShortValue) {
	    return ((ShortValue)value).shortValue();
	} else if (value instanceof IntegerValue) {
	    return ((IntegerValue)value).intValue();
	} else if (value instanceof LongValue) {
	    return ((LongValue)value).longValue();
	} else if (value instanceof FloatValue) {
	    return ((FloatValue)value).floatValue();
	} else if (value instanceof DoubleValue) {
	    return ((DoubleValue)value).doubleValue();
	} else {
	    return makeMirror((ObjectReference)value);
	}
    }
    
    public Value toValue(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Boolean) {
            return jdiVM.mirrorOf(((Boolean)object).booleanValue());
        } else if (object instanceof Byte) {
            return jdiVM.mirrorOf(((Byte)object).byteValue());
        } else if (object instanceof Character) {
            return jdiVM.mirrorOf(((Character)object).charValue());
        } else if (object instanceof Short) {
            return jdiVM.mirrorOf(((Short)object).shortValue());
        } else if (object instanceof Integer) {
            return jdiVM.mirrorOf(((Integer)object).intValue());
        } else if (object instanceof Long) {
            return jdiVM.mirrorOf(((Long)object).longValue());
        } else if (object instanceof Float) {
            return jdiVM.mirrorOf(((Float)object).floatValue());
        } else if (object instanceof Double) {
            return jdiVM.mirrorOf(((Float)object).doubleValue());
        } else {
            return ((JDIObjectMirror)object).getObjectReference();
        }
    }
    
    public ObjectReference unwrapMirror(ObjectMirror mirror) {
        return mirror == null ? null : ((JDIObjectMirror)mirror).getObjectReference();
    }
    
    @Override
    public boolean canBeModified() {
        return false; //jdiVM.canBeModified();
    }
    
    @Override
    public boolean canGetBytecodes() {
        // TODO-RS: If I refactored the API to just expose bytecode for individual methods,
        // this could be true.
        return false;
    }
    
    @Override
    public boolean hasClassInitialization() {
        return true;
    }
    
    int identityHashCode(ObjectReference object) {
        // TODO-RS: temp hack
        if (true) {
            return object.hashCode();
        }
        
        // Unfortunately we need to run code to get at these.
        // The method should be completely side-effect free though.
        ClassType systemType = (ClassType)jdiVM.classesByName(System.class.getName()).get(0);
        Method method = systemType.methodsByName("identityHashCode").get(0);
        ThreadHolograph currentThread = ThreadHolograph.currentThreadMirror();
        ThreadReference threadRef = ((JDIThreadMirror)currentThread.getWrapped()).thread;
        try {
            Value result = systemType.invokeMethod(threadRef, method, Collections.singletonList(object), ObjectReference.INVOKE_SINGLE_THREADED);
            return ((IntegerValue)result).intValue();
        } catch (InvalidTypeException e) {
            throw new InternalError(e.getMessage());
        } catch (ClassNotLoadedException e) {
            // Should never happen
            throw new RuntimeException(e);
        } catch (IncompatibleThreadStateException e) {
            // Should never happen
            throw new RuntimeException(e);
        } catch (InvocationException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public InstanceMirror makeString(String s) {
        return (InstanceMirror)makeMirror(jdiVM.mirrorOf(s));
    }
    
    @Override
    public InstanceMirror getInternedString(String s) {
        // TODO-RS
        return makeString(s);
    }
    
    @Override
    public EventDispatch dispatch() {
        return dispatch;
    }
    
    @Override
    public void addCallback(MethodMirrorHandlerRequest request, Callback<MirrorEvent> callback) {
        dispatch.addCallback(request, callback);
    }
    
    public MirrorLocation makeMirrorLocation(Location location) {
        return new JDIMirrorLocation(this, location);
    }
    
    public <T> T withoutEventRequests(Callable<T> callback) throws Exception {
        for (MirrorEventRequest request : eventRequestManager().allRequests()) {
            request.disable();
        }
        try {
            return callback.call();
        } finally {
            for (MirrorEventRequest request : eventRequestManager().allRequests()) {
                request.enable();
            }
        }
    }
    
    public Value safeInvoke(final ObjectReference o, final ThreadReference t, final Method m, final Value...values) {
        List<Value> args;
        if (values.length == 0) {
            args = Collections.emptyList();
        } else {
            args = Arrays.asList(values);
        }
        final List<Value> finalArgs = args;
        
        try {
            return withoutEventRequests(new Callable<Value>() {
                @Override
                public Value call() throws Exception {
                    return o.invokeMethod(t, m, finalArgs, 0);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<AnnotationMirror> wrapAnnotationArray(final ThreadReference thread, final ArrayReference array) {
        try {
            return withoutEventRequests(new Callable<List<AnnotationMirror>>() {
                @Override
                public List<AnnotationMirror> call() throws Exception {
                    List<AnnotationMirror> result = new ArrayList<AnnotationMirror>();
                    int size = array.length();
                    for (int i = 0; i < size; i++) {
                        result.add(new JDIAnnotationMirror(JDIVirtualMachineMirror.this, thread, (ObjectReference)array.getValue(i)));
                    }
                    return result;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public List<List<AnnotationMirror>> wrapAnnotationArrayOfArrays(ThreadReference thread, ArrayReference array) {
        List<List<AnnotationMirror>> result = new ArrayList<List<AnnotationMirror>>();
        int size = array.length();
        for (int i = 0; i < size; i++) {
            result.add(wrapAnnotationArray(thread, (ArrayReference)array.getValue(i)));
        }
        return result;
    }
    
    public Object wrapAnnotationValue(ThreadReference thread, Value value) {
        if (value instanceof ArrayReference) {
            ArrayReference array = (ArrayReference)value;
            List<Object> result = new ArrayList<Object>();
            int size = array.length();
            for (int i = 0; i < size; i++) {
                result.add(wrapAnnotationValue(thread, array.getValue(i)));
            }
            return result;
        } else if (value instanceof StringReference) {
            StringReference string = (StringReference)value;
            return string.value();
        } else if (value instanceof ObjectReference) {
            ObjectReference object = (ObjectReference)value;
            ClassType klass = (ClassType)object.referenceType();
            if (klass.isEnum()) {
                Method nameMethod = jdiVM.classesByName(Enum.class.getName()).get(0).methodsByName("name").get(0);
                StringReference name = (StringReference)safeInvoke(object, thread, nameMethod);
                return new EnumMirror(makeClassMirror(klass), name.value());
            } else {
                return new JDIAnnotationMirror(this, thread, object);
            }
        } else { // Primitive value
            return wrapValue(value);
        }
    }
}
