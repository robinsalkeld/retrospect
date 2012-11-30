package edu.ubc.mirrors.asjdi;

import java.util.List;

import com.sun.jdi.ClassType;
import com.sun.jdi.InterfaceType;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.asjdi.MirrorsReferenceType;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsInterfaceType extends MirrorsReferenceType implements InterfaceType {

    public MirrorsInterfaceType(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
        assert wrapped.isInterface();
    }

    @Override
    public List<ClassType> implementors() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<InterfaceType> subinterfaces() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<InterfaceType> superinterfaces() {
        return interfaces();
    }
}
