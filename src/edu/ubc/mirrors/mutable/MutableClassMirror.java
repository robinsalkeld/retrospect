package edu.ubc.mirrors.mutable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mat.snapshot.model.IObject;

import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;

public class MutableClassMirror extends ClassMirror {

    private final InstanceMirror mutableLayer;
    private final ClassMirror immutableClassMirror;
    
    public MutableClassMirror(ClassMirror immutableClassMirror) {
        this.mutableLayer = new FieldMapMirror(null);
        this.immutableClassMirror = immutableClassMirror;
    }
    
    @Override
    public String getClassName() {
        return immutableClassMirror.getClassName();
    }

    @Override
    public ClassMirrorLoader getLoader() {
        // TODO
        return null;
    }

    @Override
    public byte[] getBytecode() {
        return immutableClassMirror.getBytecode();
    }

    @Override
    public boolean isArray() {
        return immutableClassMirror.isArray();
    }

    @Override
    public ClassMirror getComponentClassMirror() {
        return immutableClassMirror.getComponentClassMirror();
    }

    @Override
    public ClassMirror getSuperClassMirror() {
        return immutableClassMirror.getSuperClassMirror();
    }

    @Override
    public boolean isInterface() {
        return immutableClassMirror.isInterface();
    }

    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        return immutableClassMirror.getInterfaceMirrors();
    }

    @Override
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        return new MutableFieldMirror(mutableLayer.getMemberField(name), immutableClassMirror.getStaticField(name));
    }

    private static final Map<ObjectMirror, ObjectMirror> mirrors = new HashMap<ObjectMirror, ObjectMirror>();
    
    public static ObjectMirror makeMirror(ObjectMirror immutableMirror) {
        if (immutableMirror == null) {
            return null;
        }
        
        ObjectMirror result = mirrors.get(immutableMirror);
        if (result != null) {
            return result;
        }
        
        if (immutableMirror instanceof InstanceMirror) {
            result = new MutableInstanceMirror((InstanceMirror)immutableMirror);
        } else if (immutableMirror instanceof ObjectArrayMirror) {
            result = new MutableObjectArrayMirror((ObjectArrayMirror)immutableMirror);
        // TODO: fix - should check class name instead!
        } else if (immutableMirror instanceof CharArrayMirror){
            result = new MutableCharArrayMirror((CharArrayMirror)immutableMirror);
        } else {
            throw new IllegalArgumentException("Unsupported subclass: " + immutableMirror.getClass());
        }
        
        mirrors.put(immutableMirror, result);
        return result;
    }
}
