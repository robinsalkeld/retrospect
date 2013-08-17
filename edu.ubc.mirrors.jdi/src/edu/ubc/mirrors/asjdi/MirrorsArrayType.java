package edu.ubc.mirrors.asjdi;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.asjdi.MirrorsReferenceType;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsArrayType extends MirrorsReferenceType implements ArrayType {

    public MirrorsArrayType(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
        assert wrapped.isArray();
    }

    @Override
    public String componentSignature() {
        return wrapped.getComponentClassMirror().getClassName();
    }

    @Override
    public Type componentType() throws ClassNotLoadedException {
        return vm.typeForClassMirror(wrapped.getComponentClassMirror());
    }

    @Override
    public String componentTypeName() {
        return wrapped.getComponentClassMirror().getClassName();
    }

    @Override
    public ArrayReference newInstance(int size) {
        throw new UnsupportedOperationException();
    }

}
