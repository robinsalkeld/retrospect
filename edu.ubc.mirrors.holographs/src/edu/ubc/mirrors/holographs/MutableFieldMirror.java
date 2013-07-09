package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.wrapping.WrappingFieldMirror;

public class MutableFieldMirror extends WrappingFieldMirror {

    private final ClassHolograph klass;
    
    private FieldMirror bytecodeField;
    
    public MutableFieldMirror(ClassHolograph klass, FieldMirror immutableFieldMirror) {
        super(klass.getVM(), immutableFieldMirror);
        this.klass = klass;
    }
    
    @Override
    public ClassMirror getType() {
        if (klass.hasBytecode()) {
            return super.getType();
        } else {
            return getBytecodeField().getType();
        }
    }
    
    @Override
    public String getTypeName() {
        if (klass.hasBytecode()) {
            return super.getTypeName();
        } else {
            return getBytecodeField().getTypeName();
        }
    }
    
    @Override
    public int getModifiers() {
        if (klass.hasBytecode()) {
            return super.getModifiers();
        } else {
            return getBytecodeField().getModifiers();
        }
    }
    
    private FieldMirror getBytecodeField() {
        if (bytecodeField == null) {
            bytecodeField = klass.getBytecodeMirror().getDeclaredField(getName());
        }
        return bytecodeField;
    }
}
