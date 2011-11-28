package edu.ubc.mirrors;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

public class MirageMethodGenerator extends MethodVisitor {

    private LocalVariablesSorter lvs;
    
    public MirageMethodGenerator(MethodVisitor mv) {
        super(Opcodes.ASM4, mv);
    }

    public void setLocalVariablesSorter(LocalVariablesSorter lvs) {
        this.lvs = lvs;
    }
    
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        Type fieldType = Type.getType(desc);
        int fieldSort = fieldType.getSort();
        String suffix = "";
        switch (fieldSort) {
            case Type.BOOLEAN: suffix = "Boolean"; break;
            case Type.BYTE: suffix = "Byte"; break;
            case Type.CHAR: suffix = "Char"; break;
            case Type.SHORT: suffix = "Short"; break;
            case Type.INT: suffix = "Int"; break;
            case Type.LONG: suffix = "Long"; break;
            case Type.FLOAT: suffix = "Float"; break;
            case Type.DOUBLE: suffix = "Double"; break;
            case Type.ARRAY: 
            case Type.OBJECT: 
                break;
            default:
                throw new IllegalStateException("Bad sort: " + fieldSort);
        }
        
        boolean isSet;
        boolean isStatic;
        switch (opcode) {
            case Opcodes.GETFIELD: 
                isSet = false;
                isStatic = false;
                break;
            case Opcodes.GETSTATIC:
                isSet = false;
                isStatic = true;
                break;
            case Opcodes.PUTFIELD:
                isSet = true;
                isStatic = false;
                break;
            case Opcodes.PUTSTATIC:
                isSet = true;
                isStatic = true;
                break;
            default:
                throw new IllegalArgumentException("Bad opcode: " + opcode);
        }
        
        // Pop the original argument
        int setValueLocal = lvs.newLocal(fieldType);
        if (isSet) {
            super.visitVarInsn(fieldType.getOpcode(Opcodes.ISTORE), setValueLocal);
        }
        
        // Push the mirror instance onto the stack
        super.visitFieldInsn(Opcodes.GETFIELD, 
                             Type.getInternalName(ObjectMirage.class), 
                             "mirror", 
                             MirageClassGenerator.objectMirrorType.getDescriptor());
        
        // Get the field mirror onto the stack
        super.visitLdcInsn(name);
        super.visitMethodInsn(Opcodes.INVOKEINTERFACE, 
                              MirageClassGenerator.objectMirageType.getInternalName(), 
                              isStatic ? "getStaticField" : "getMemberField", 
                              Type.getMethodDescriptor(MirageClassGenerator.fieldMirrorType, Type.getType(String.class)));
        
        // Call the appropriate getter/setter method on the mirror
        String methodDesc;
        if (isSet) {
            super.visitVarInsn(fieldType.getOpcode(Opcodes.ILOAD), setValueLocal);
            methodDesc = Type.getMethodDescriptor(Type.VOID_TYPE, fieldType);
        } else {
            methodDesc = Type.getMethodDescriptor(fieldType);
        }
        super.visitMethodInsn(Opcodes.INVOKEINTERFACE, 
                              MirageClassGenerator.fieldMirrorType.getInternalName(), 
                              (isSet ? "set" : "get") + suffix, 
                              methodDesc);
    }
}
