package edu.ubc.mirrors.asjdi;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.asjdi.MirrorsObjectReference;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public abstract class MirrorsInstanceReference extends MirrorsObjectReference {

    private final InstanceMirror wrapped;
    
    public MirrorsInstanceReference(MirrorsVirtualMachine vm, InstanceMirror wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    protected InstanceMirror readField(ClassMirror klass, String fieldName) {
        try {
            return (InstanceMirror)wrapped.get(klass.getDeclaredField(fieldName));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
