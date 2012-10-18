package edu.ubc.mirrors.wrapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.DoubleArrayMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FloatArrayMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.LongArrayMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ShortArrayMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.test.Breakpoint;

public abstract class WrappingVirtualMachine implements VirtualMachineMirror {

    protected final VirtualMachineMirror wrappedVM;
    
    public WrappingVirtualMachine(VirtualMachineMirror wrappedVM) {
        this.wrappedVM = wrappedVM;
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
        
        String classNameString = mirror.getClassMirror().getClassName();
        
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
            if (((ClassMirror)mirror).isPrimitive()) {
        	Breakpoint.bp();
            }
            return new WrappingClassMirror(this, (ClassMirror)mirror);
        } else if (mirror instanceof ClassMirrorLoader) {
            return new WrappingClassMirrorLoader(this, (ClassMirrorLoader)mirror);
        } else if (mirror instanceof ThreadMirror) {
            return new WrappingThreadMirror(this, (ThreadMirror)mirror);
        } else if (mirror instanceof InstanceMirror) {
            return new WrappingInstanceMirror(this, (InstanceMirror)mirror);
        } else if (mirror instanceof ObjectArrayMirror) {
            return new WrappingObjectArrayMirror(this, (ObjectArrayMirror)mirror);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    protected ObjectMirror unwrapMirror(ObjectMirror mirror) {
        return mirror == null ? null : ((WrappingMirror)mirror).wrapped;
    }

    protected ClassMirror unwrapClassMirror(ObjectMirror mirror) {
        return (ClassMirror)unwrapMirror(mirror);
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
    
    public List<FieldMirror> getWrappedFieldList(List<FieldMirror> fields) {
        List<FieldMirror> result = new ArrayList<FieldMirror>(fields.size());
        for (FieldMirror field : fields) {
            result.add(getFieldMirror(field));
        }
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
	return new WrappingMethodMirror(this, method);
    }

    public Object wrapValue(Object value) {
        if (value instanceof ObjectMirror) {
            return getWrappedMirror((ObjectMirror)value);
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
    
    
}
