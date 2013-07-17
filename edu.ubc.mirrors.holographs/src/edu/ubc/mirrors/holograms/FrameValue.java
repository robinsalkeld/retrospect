package edu.ubc.mirrors.holograms;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Value;

public class FrameValue implements Value {

    private final BasicValue value;
    
    /**
     */
    private final AbstractInsnNode newInsn;
    
    private static final AbstractInsnNode UNINITIALIZED_THIS_INSN = new TypeInsnNode(Opcodes.NEW, null);
    private static final AbstractInsnNode INITIALIZED = null;
    
    public FrameValue(BasicValue value, AbstractInsnNode newInsn) {
        this.value = value;
        this.newInsn = newInsn;
    }
    
    public BasicValue getBasicValue() {
        return value;
    }
    
    public AbstractInsnNode getNewInsn() {
        return newInsn;
    }
    
    public static FrameValue fromBasicValue(BasicValue value) {
        return value == null ? null : new FrameValue(value, INITIALIZED);
    }
    
    public static FrameValue makeUninitializedThis(BasicValue value) {
        return new FrameValue(value, UNINITIALIZED_THIS_INSN);
    }
    
    public boolean isUninitialized() {
        return newInsn != INITIALIZED;
    }
    
    public boolean isUninitializedThis() {
        return newInsn == UNINITIALIZED_THIS_INSN;
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
        return value.equals(other.value) && newInsn == other.newInsn; 
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
