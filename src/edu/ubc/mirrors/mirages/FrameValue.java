package edu.ubc.mirrors.mirages;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Value;

public class FrameValue implements Value {

    private final BasicValue value;
    
    /**
     */
    private final int offsetOfNew;
    
    private static final int UNINITIALIZED_THIS_OFFSET = -1;
    private static final int INITIALIZED_OFFSET = -2;
    
    public FrameValue(BasicValue value, int offsetOfNew) {
        this.value = value;
        this.offsetOfNew = offsetOfNew;
    }
    
    public BasicValue getBasicValue() {
        return value;
    }
    
    public static FrameValue fromBasicValue(BasicValue value) {
        return value == null ? null : new FrameValue(value, INITIALIZED_OFFSET);
    }
    
    public static FrameValue makeUninitializedThis(BasicValue value) {
        return new FrameValue(value, UNINITIALIZED_THIS_OFFSET);
    }
    
    public boolean isUninitialized() {
        return offsetOfNew != INITIALIZED_OFFSET;
    }
    
    public boolean isUninitializedThis() {
        return offsetOfNew == UNINITIALIZED_THIS_OFFSET;
    }
    
    public void addToFrameObjects(Object[] objects, int index) {
        if (isUninitializedThis()) {
            objects[index] = Opcodes.UNINITIALIZED_THIS;
        } else if (isUninitialized()) {
            objects[index] = offsetOfNew;
        } else {
            Type type = value.getType();
            if (type == null) {
                objects[index] = Opcodes.TOP;
            } else {
                switch (type.getSort()) {
                case Type.VOID:
                    // TODO: Not sure what to do here. Used for BasicValue.RETURNADDRESS_VALUE,
                    // but I can't figure out the correct verifier type for the return addresses
                    // used by JSR/RET.
                    objects[index] = Opcodes.TOP;
                case Type.BOOLEAN:
                case Type.BYTE:
                case Type.CHAR:
                case Type.SHORT:
                case Type.INT:
                    objects[index] = Opcodes.INTEGER;
                case Type.LONG:
                    objects[index] = Opcodes.LONG;
                case Type.FLOAT:
                    objects[index] = Opcodes.FLOAT;
                    objects[index + 1] = Opcodes.TOP;
                case Type.DOUBLE:
                    objects[index] = Opcodes.DOUBLE;
                    objects[index + 1] = Opcodes.TOP;
                case Type.OBJECT:
                case Type.ARRAY:
                    objects[index] = type.getInternalName();
                case Type.METHOD:
                    // Shouldn't happen
                default:
                    throw new RuntimeException("Bad sort: " + type.getSort());
                }
            }
        }
    }
    
    @Override
    public int getSize() {
        return value.getSize();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FrameValue)) {
            return false;
        }
        
        FrameValue other = (FrameValue)obj;
        return value.equals(other.value) && offsetOfNew == other.offsetOfNew; 
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    @Override
    public String toString() {
        if (isUninitializedThis()) {
            return "(uninitialized this)";
        } else if (isUninitialized()) {
            return "(uninitialized) " + value;
        } else {
            return value.toString();
        }
    }
}
