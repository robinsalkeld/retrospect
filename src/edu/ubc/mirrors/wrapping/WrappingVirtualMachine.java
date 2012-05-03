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
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ShortArrayMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.mirages.BooleanArrayMirage;
import edu.ubc.mirrors.mirages.ByteArrayMirage;
import edu.ubc.mirrors.mirages.CharArrayMirage;
import edu.ubc.mirrors.mirages.DoubleArrayMirage;
import edu.ubc.mirrors.mirages.FloatArrayMirage;
import edu.ubc.mirrors.mirages.IntArrayMirage;
import edu.ubc.mirrors.mirages.LongArrayMirage;
import edu.ubc.mirrors.mirages.ShortArrayMirage;
import edu.ubc.mirrors.mutable.MutableBooleanArrayMirror;
import edu.ubc.mirrors.mutable.MutableByteArrayMirror;
import edu.ubc.mirrors.mutable.MutableCharArrayMirror;
import edu.ubc.mirrors.mutable.MutableClassMirror;
import edu.ubc.mirrors.mutable.MutableClassMirrorLoader;
import edu.ubc.mirrors.mutable.MutableDoubleArrayMirror;
import edu.ubc.mirrors.mutable.MutableFloatArrayMirror;
import edu.ubc.mirrors.mutable.MutableInstanceMirror;
import edu.ubc.mirrors.mutable.MutableIntArrayMirror;
import edu.ubc.mirrors.mutable.MutableLongArrayMirror;
import edu.ubc.mirrors.mutable.MutableObjectArrayMirror;
import edu.ubc.mirrors.mutable.MutableShortArrayMirror;
import edu.ubc.mirrors.mutable.MutableThreadMirror;

public abstract class WrappingVirtualMachine implements VirtualMachineMirror {

    private final VirtualMachineMirror wrappedVM;
    
    public WrappingVirtualMachine(VirtualMachineMirror wrappedVM) {
        this.wrappedVM = wrappedVM;
    }
    
    @Override
    public ClassMirror findBootstrapClassMirror(String name) {
        return (ClassMirror)getWrappedMirror(wrappedVM.findBootstrapClassMirror(name));
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
            // Primitive arrays don't have to be wrapped (or at least we have no use for wrapping them yet).
            return mirror;
        }
    }
    
    protected ObjectMirror unwrapMirror(ObjectMirror mirror) {
        return mirror instanceof WrappingMirror ? ((WrappingMirror)mirror).wrapped : mirror;
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
    
    public WrappingClassMirrorLoader getWrappedClassLoaderMirror(ClassMirrorLoader mirror) {
        return (WrappingClassMirrorLoader)getWrappedMirror(mirror);
    }
    
    @Override
    public List<ClassMirror> findAllClasses(String name) {
        return getWrappedClassMirrorList(wrappedVM.findAllClasses(name));
    }
    
    // TODO-RS: Should these be full-on object mirrors as well? Probably. :(
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
        return getWrappedClassMirror(wrappedVM.getArrayClass(dimensions, elementClass));
    }
}
