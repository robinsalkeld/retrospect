package edu.ubc.mirrors.tod;

import tod.core.database.structure.IFieldInfo;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;

public class TODFieldMirror implements FieldMirror {

    private final TODVirtualMachineMirror vm;
    protected final IFieldInfo field;

    public TODFieldMirror(TODVirtualMachineMirror vm, IFieldInfo field) {
        super();
        this.vm = vm;
        this.field = field;
    }

    @Override
    public ClassMirror getDeclaringClass() {
        return vm.makeMirror(field.getDeclaringType());
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public String getTypeName() {
        return field.getType().getName();
    }
    
    @Override
    public ClassMirror getType() {
        return vm.makeMirror(field.getType());
    }

    @Override
    public int getModifiers() {
        throw new UnsupportedOperationException();
    }
    
}
