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
package edu.ubc.mirrors.wrapping;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.DoubleArrayMirror;
import edu.ubc.mirrors.EventDispatch;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FloatArrayMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.InvocableMirror;
import edu.ubc.mirrors.LongArrayMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorLocation;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ShortArrayMirror;
import edu.ubc.mirrors.StaticFieldValuesMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public abstract class WrappingVirtualMachine implements VirtualMachineMirror {

    protected final VirtualMachineMirror wrappedVM;
    private final EventDispatch dispatch;
    
    public WrappingVirtualMachine(VirtualMachineMirror wrappedVM) {
        this.wrappedVM = wrappedVM;
        this.dispatch = new EventDispatch(this);
    }
    
    public VirtualMachineMirror getWrappedVM() {
        return wrappedVM;
    }
    
    @Override
    public ClassMirror findBootstrapClassMirror(String name) {
        return (ClassMirror)getWrappedMirror(wrappedVM.findBootstrapClassMirror(name));
    }

    @Override
    public ClassMirror defineBootstrapClass(String name, ByteArrayMirror b, int off, int len) {
        ByteArrayMirror unwrappedB = (ByteArrayMirror)unwrapMirror(b);
        return getWrappedClassMirror(wrappedVM.defineBootstrapClass(name, unwrappedB, off, len));
    }
    
    @Override
    public Enumeration<URL> findBootstrapResources(String path) throws IOException {
        return wrappedVM.findBootstrapResources(path);
    }
    
    // It would be great to make this a WeakHashMap, but since the wrapping objects may have state they can't
    // be soundly thrown away and recreated on demand. Would need to be smarter about permanently storing
    // wrappers after first modifying them.
    private final Map<ObjectMirror, ObjectMirror> wrappedMirrors = new HashMap<ObjectMirror, ObjectMirror>();
    
    public ObjectMirror getWrappedMirror(ObjectMirror mirror) {
        if (mirror == null) {
            return null;
        }
        
        ObjectMirror result = wrappedMirrors.get(mirror);
        if (result != null) {
            return result;
        }
        
        result = wrapMirror(mirror);
        
        wrappedMirrors.put(mirror, result);
        return result;
    }

    protected ObjectMirror wrapMirror(ObjectMirror mirror) {
        if (mirror == null) {
            return null;
        }
        
        String classNameString = mirror.getClassMirror().getSignature();
        
        if (classNameString.equals("[Z")) {
            return new WrappingBooleanArrayMirror(this, (BooleanArrayMirror)mirror);
        } else if (classNameString.equals("[B")) {
            return new WrappingByteArrayMirror(this, (ByteArrayMirror)mirror);
        } else if (classNameString.equals("[C")) {
            return new WrappingCharArrayMirror(this, (CharArrayMirror)mirror);
        } else if (classNameString.equals("[S")) {
            return new WrappingShortArrayMirror(this, (ShortArrayMirror)mirror);
        } else if (classNameString.equals("[I")) {
            return new WrappingIntArrayMirror(this, (IntArrayMirror)mirror);
        } else if (classNameString.equals("[J")) {
            return new WrappingLongArrayMirror(this, (LongArrayMirror)mirror);
        } else if (classNameString.equals("[F")) {
            return new WrappingFloatArrayMirror(this, (FloatArrayMirror)mirror);
        } else if (classNameString.equals("[D")) {
            return new WrappingDoubleArrayMirror(this, (DoubleArrayMirror)mirror);
        } else if (mirror instanceof ClassMirror) {
            return new WrappingClassMirror(this, (ClassMirror)mirror);
        } else if (mirror instanceof ClassMirrorLoader) {
            return new WrappingClassMirrorLoader(this, (ClassMirrorLoader)mirror);
        } else if (mirror instanceof ThreadMirror) {
            return new WrappingThreadMirror(this, (ThreadMirror)mirror);
        } else if (mirror instanceof StaticFieldValuesMirror) {
            return new WrappingStaticFieldValuesMirror(this, (StaticFieldValuesMirror)mirror);
        } else if (mirror instanceof InstanceMirror) {
            return new WrappingInstanceMirror(this, (InstanceMirror)mirror);
        } else if (mirror instanceof ObjectArrayMirror) {
            return new WrappingObjectArrayMirror(this, (ObjectArrayMirror)mirror);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    public ObjectMirror unwrapMirror(ObjectMirror mirror) {
        return mirror == null ? null : ((WrappingMirror)mirror).wrapped;
    }

    protected ClassMirror unwrapClassMirror(ObjectMirror mirror) {
        return (ClassMirror)unwrapMirror(mirror);
    }

    public InstanceMirror unwrapInstanceMirror(ObjectMirror mirror) {
        return (InstanceMirror)unwrapMirror(mirror);
    }

    public FieldMirror unwrapFieldMirror(FieldMirror mirror) {
        return ((WrappingFieldMirror)mirror).wrapped;
    }

    public WrappingClassMirror getWrappedClassMirror(ClassMirror mirror) {
        return (WrappingClassMirror)getWrappedMirror(mirror);
    }
    
    public List<ClassMirror> getWrappedClassMirrorList(List<ClassMirror> list) {
        List<ClassMirror> result = new ArrayList<ClassMirror>(list.size());
        for (ClassMirror c : list) {
            result.add((ClassMirror)getWrappedMirror(c));
        }
        return result;
    }
    
    public List<ThreadMirror> getWrappedThreadMirrorList(List<ThreadMirror> list) {
        List<ThreadMirror> result = new ArrayList<ThreadMirror>(list.size());
        for (ThreadMirror c : list) {
            result.add((ThreadMirror)getWrappedMirror(c));
        }
        return result;
    }
    
    public WrappingClassMirrorLoader getWrappedClassLoaderMirror(ClassMirrorLoader mirror) {
        return (WrappingClassMirrorLoader)getWrappedMirror(mirror);
    }
    
    @Override
    public List<ClassMirror> findAllClasses() {
        return getWrappedClassMirrorList(wrappedVM.findAllClasses());
    }
    
    @Override
    public List<ClassMirror> findAllClasses(String name, boolean includeSubclasses) {
        return getWrappedClassMirrorList(wrappedVM.findAllClasses(name, includeSubclasses));
    }
    
    @Override
    public List<ThreadMirror> getThreads() {
        return getWrappedThreadMirrorList(wrappedVM.getThreads());
    }
    
    private final Map<FieldMirror, FieldMirror> wrappedFieldMirrors = new HashMap<FieldMirror, FieldMirror>();
    
    protected FieldMirror wrapFieldMirror(FieldMirror fieldMirror) {
        return new WrappingFieldMirror(this, fieldMirror);
    }
    
    public FieldMirror getFieldMirror(FieldMirror fieldMirror) {
        if (fieldMirror == null) {
            return null;
        }
        
        FieldMirror result = wrappedFieldMirrors.get(fieldMirror);
        if (result != null) {
            return result;
        }
        
        result = wrapFieldMirror(fieldMirror);
        
        wrappedFieldMirrors.put(fieldMirror, result);
        return result;
    }
    
    @Override
    public ClassMirror getPrimitiveClass(String name) {
        return getWrappedClassMirror(wrappedVM.getPrimitiveClass(name));
    }
    
    @Override
    public ClassMirror getArrayClass(int dimensions, ClassMirror elementClass) {
        ClassMirror unwrapedElementClass = (ClassMirror)unwrapMirror(elementClass);
        return getWrappedClassMirror(wrappedVM.getArrayClass(dimensions, unwrapedElementClass));
    }
    
    @Override
    public MirrorEventRequestManager eventRequestManager() {
        return new WrappingMirrorEventRequestManager(this, wrappedVM.eventRequestManager());
    }
    
    @Override
    public MirrorEventQueue eventQueue() {
	return new WrappingMirrorEventQueue(this, wrappedVM.eventQueue()); 
    }

    @Override
    public void suspend() {
        wrappedVM.suspend();
    }
    
    @Override
    public void resume() {
	wrappedVM.resume(); 
    }

    public MethodMirror wrapMethod(MethodMirror method) {
	return method == null ? null : new WrappingMethodMirror(this, method);
    }

    public Object wrapValue(Object value) {
        if (value instanceof ObjectMirror) {
            return getWrappedMirror((ObjectMirror)value);
        } else if (value instanceof AnnotationMirror) {
            return wrapAnnotation((AnnotationMirror)value);
        } else {
            return value;
        }
    }
    
    public Object unwrappedValue(Object value) {
        if (value instanceof ObjectMirror) {
            return unwrapMirror((ObjectMirror)value);
        } else {
            return value;
        }
    }

    public ConstructorMirror wrapConstructor(ConstructorMirror constructor) {
	return new WrappingConstructorMirror(this, constructor);
    }
    
    @Override
    public boolean canBeModified() {
        return wrappedVM.canBeModified();
    }

    @Override
    public boolean canGetBytecodes() {
        return wrappedVM.canGetBytecodes();
    }
    
    @Override
    public boolean hasClassInitialization() {
        return wrappedVM.hasClassInitialization();
    }

    public FrameMirror wrapFrameMirror(WrappingVirtualMachine vm, FrameMirror frame) {
        return new WrappingFrameMirror(vm, frame);
    }
    
    // Introducing a single choke point 
    public int identityHashCode(WrappingMirror wrapper) {
        return wrapper.identityHashCode();
    }

    public List<AnnotationMirror> wrapAnnotations(List<AnnotationMirror> annotations) {
        List<AnnotationMirror> result = new ArrayList<AnnotationMirror>();
        for (AnnotationMirror a : annotations) {
            result.add(wrapAnnotation(a));
        }
        return result;
    }
    
    public List<List<AnnotationMirror>> wrapAnnotationsList(List<List<AnnotationMirror>> annotationsList) {
        List<List<AnnotationMirror>> result = new ArrayList<List<AnnotationMirror>>();
        for (List<AnnotationMirror> list : annotationsList) {
            result.add(wrapAnnotations(list));
        }
        return result;
    }
    
    public AnnotationMirror wrapAnnotation(AnnotationMirror a) {
        return new WrappingAnnotationMirror(this, a);
    }
    
    @Override
    public InstanceMirror makeString(String s) {
        return (InstanceMirror)getWrappedMirror(wrappedVM.makeString(s));
    }
    
    @Override
    public InstanceMirror getInternedString(String s) {
        return (InstanceMirror)getWrappedMirror(wrappedVM.getInternedString(s));
    }
    
    @Override
    public EventDispatch dispatch() {
        return dispatch;
    }

    @Override
    public void addCallback(MirrorEventRequest request, Callback<MirrorEvent> callback) {
        dispatch.addCallback(request, callback);
    }
    
    public MirrorLocation wrapLocation(MirrorLocation location) {
        return new WrappingMirrorLocation(this, location);
    }

    public MethodMirror unwrapMethodMirror(MethodMirror method) {
        return ((WrappingMethodMirror)method).wrapped;
    }

    public ConstructorMirror unwrapConstructorMirror(ConstructorMirror constructor) {
        return ((WrappingConstructorMirror)constructor).wrapped;
    }

    public MirrorInvocationHandler unwrapInvocationHandler(MirrorInvocationHandler handler) {
        return ((WrappingMirrorInvocationHandler)handler).wrapped;
    }

    public ThreadMirror unwrapThread(ThreadMirror thread) {
        return ((WrappingThreadMirror)thread).wrappedThread;
    }

    public InvocableMirror unwrapInvocable(InvocableMirror invocable) {
        if (invocable instanceof ConstructorMirror) {
            return unwrapConstructorMirror((ConstructorMirror)invocable);
        } else {
            return unwrapMethodMirror((MethodMirror)invocable);
        }
    }
    
    public List<Object> wrapValueList(List<Object> values) {
        List<Object> result = new ArrayList<Object>();
        for (Object a : values) {
            result.add(wrapValue(a));
        }
        return result;
    }
}
