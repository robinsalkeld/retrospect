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
        try {
            return super.getType();
        } catch (UnsupportedOperationException e) {
            return getBytecodeField().getType();
        }
    }
    
    @Override
    public String getTypeName() {
        try {
            return super.getTypeName();
        } catch (UnsupportedOperationException e) {
            return getBytecodeField().getTypeName();
        }
    }
    
    @Override
    public int getModifiers() {
        try {
            return super.getModifiers();
        } catch (UnsupportedOperationException e) {
            return getBytecodeField().getModifiers();
        }
    }
    
    private FieldMirror getBytecodeField() {
        if (bytecodeField == null) {
            try {
                bytecodeField = klass.getBytecodeMirror().getDeclaredField(getName());
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        return bytecodeField;
    }
}
