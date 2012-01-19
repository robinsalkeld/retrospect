package edu.ubc.mirrors;

import static edu.ubc.mirrors.MirageClassGenerator.fieldMapMirrorType;
import static edu.ubc.mirrors.MirageClassGenerator.getMirageInternalClassName;
import static edu.ubc.mirrors.MirageClassGenerator.nativeObjectMirrorType;
import static edu.ubc.mirrors.MirageClassGenerator.objectMirageType;
import static edu.ubc.mirrors.NativeClassGenerator.getNativeInternalClassName;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

public class MirageMethodGenerator extends MethodVisitor {

    private LocalVariablesSorter lvs;
    private final String superName;
    private final Type methodType;
    private final boolean isToString;
    
    public MirageMethodGenerator(MethodVisitor mv, String superName, Type methodType, boolean isToString) {
        super(Opcodes.ASM4, mv);
        this.superName = superName;
        this.methodType = methodType;
        this.isToString = isToString;
    }

    public void setLocalVariablesSorter(LocalVariablesSorter lvs) {
        this.lvs = lvs;
    }
    
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (name.equals("<init>")) {
            if (owner.equals(Type.getInternalName(Object.class))) {
                owner = objectMirageType.getInternalName();
            }
            String originalType = MirageClassGenerator.getOriginalClassName(owner);
            String nativeType = NativeClassGenerator.getNativeInternalClassName(originalType);
            desc = MirageClassGenerator.addMirrorArgToDesc(desc);
            
            if (owner.equals(superName)) {
                // If we're calling super(...), just push the extra mirror argument on the stack
                // TODO-RS: What if a subclass constructs a superclass instance in its constructor???
                super.visitVarInsn(Opcodes.ALOAD, methodType.getArgumentTypes().length);
            } else {
                // Otherwise construct it
                super.visitTypeInsn(Opcodes.NEW, fieldMapMirrorType.getInternalName());
                super.visitInsn(Opcodes.DUP);
                super.visitLdcInsn(Type.getObjectType(nativeType));
                super.visitMethodInsn(Opcodes.INVOKESPECIAL, 
                        fieldMapMirrorType.getInternalName(), 
                        "<init>", 
                        Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Class.class)));
            }
        }
            
        super.visitMethodInsn(opcode, owner, name, desc);
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
                fieldType = Type.getType(Object.class);
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
        
        if (isStatic) {
            super.visitFieldInsn(opcode, 
                                 MirageClassGenerator.getMirageInternalClassName(owner), 
                                 name, 
                                 MirageClassGenerator.getMirageType(Type.getType(desc)).getDescriptor());
        } else {
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
                                  MirageClassGenerator.objectMirrorType.getInternalName(), 
                                  isStatic ? "getStaticField" : "getMemberField", 
                                  Type.getMethodDescriptor(MirageClassGenerator.fieldMirrorType, MirageClassGenerator.getMirageType(Type.getType(String.class))));
            
            // Call the appropriate getter/setter method on the mirror
            String methodDesc;
            Type fieldMirageType = MirageClassGenerator.getMirageType(fieldType);
            if (isSet) {
                super.visitVarInsn(fieldType.getOpcode(Opcodes.ILOAD), setValueLocal);
                methodDesc = Type.getMethodDescriptor(Type.VOID_TYPE, fieldMirageType);
            } else {
                methodDesc = Type.getMethodDescriptor(fieldMirageType);
            }
            super.visitMethodInsn(Opcodes.INVOKEINTERFACE, 
                    MirageClassGenerator.fieldMirrorType.getInternalName(), 
                    (isSet ? "set" : "get") + suffix, 
                    methodDesc);
        }
    }
    
    @Override
    public void visitInsn(int opcode) {
        if (isToString && opcode == Opcodes.ARETURN) {
            super.visitMethodInsn(Opcodes.INVOKESTATIC, 
                                  MirageClassGenerator.objectMirageType.getInternalName(),
                                  "getRealStringForMirage",
                                  Type.getMethodDescriptor(Type.getType(String.class), Type.getType(ObjectMirage.class)));
        }
        
        super.visitInsn(opcode);
    }
    
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        // TODO calculate this more precisely (or get ASM to do it for me)
        super.visitMaxs(maxStack + 10, maxLocals);
    }
}
