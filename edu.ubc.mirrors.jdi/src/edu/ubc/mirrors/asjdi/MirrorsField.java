package edu.ubc.mirrors.asjdi;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.asjdi.MirrorsMirrorWithModifiers;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsField extends MirrorsMirrorWithModifiers implements Field {

    protected final FieldMirror wrapped;
    
    public MirrorsField(MirrorsVirtualMachine vm, FieldMirror wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public ReferenceType declaringType() {
        return (ReferenceType)vm.typeForClassMirror(wrapped.getDeclaringClass());
    }

    @Override
    public String genericSignature() {
        return null;
    }

    @Override
    public String name() {
        return wrapped.getName();
    }

    @Override
    public String signature() {
        return Reflection.typeForClassMirror(wrapped.getType()).getDescriptor();
    }

    @Override
    public int modifiers() {
        return wrapped.getModifiers();
    }

    @Override
    public int compareTo(Field o) {
        return name().compareTo(o.name());
    }

    @Override
    public boolean isEnumConstant() {
        return (modifiers() & 0x00004000) != 0;
    }

    @Override
    public Type type() throws ClassNotLoadedException {
        return vm.typeForClassMirror(wrapped.getType());
    }

    @Override
    public String typeName() {
        return wrapped.getTypeName();
    }

}
