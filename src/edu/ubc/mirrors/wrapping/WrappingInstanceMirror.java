package edu.ubc.mirrors.wrapping;

import java.util.List;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;

public class WrappingInstanceMirror extends WrappingMirror implements InstanceMirror {

    protected final InstanceMirror wrappedInstance;
    
    public WrappingInstanceMirror(WrappingVirtualMachine vm, InstanceMirror wrappedInstance) {
        super(vm, wrappedInstance);
        this.wrappedInstance = wrappedInstance;
    }
    
    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        return vm.getFieldMirror(wrappedInstance.getMemberField(name));
    }

    @Override
    public List<FieldMirror> getMemberFields() {
        return vm.getWrappedFieldList(wrappedInstance.getMemberFields());
    }

}
