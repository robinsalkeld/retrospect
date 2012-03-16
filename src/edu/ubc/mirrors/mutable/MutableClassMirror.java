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

    private final MutableClassMirrorLoader loader;
    private final InstanceMirror mutableStaticFields;
    private final InstanceMirror mutableMemberFields;
    private final ClassMirror immutableClassMirror;
    
    public MutableClassMirror(MutableClassMirrorLoader loader, ClassMirror immutableClassMirror) {
        this.loader = loader;
        this.mutableStaticFields = new FieldMapMirror(null);
        this.mutableMemberFields = new FieldMapMirror(null);
        this.immutableClassMirror = immutableClassMirror;
    }
    
    @Override
    public String getClassName() {
        return immutableClassMirror.getClassName();
    }

    @Override
    public ClassMirrorLoader getLoader() {
        return loader;
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
        return new MutableFieldMirror(loader, mutableStaticFields.getMemberField(name), immutableClassMirror.getStaticField(name));
    }

    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        return new MutableFieldMirror(loader, mutableMemberFields.getMemberField(name), immutableClassMirror.getMemberField(name));
    }
    
    @Override
    public boolean isPrimitive() {
        return immutableClassMirror.isPrimitive();
    }
    
    @Override
    public Class<?> getNativeStubsClass() {
        return immutableClassMirror.getNativeStubsClass();
    }
}
