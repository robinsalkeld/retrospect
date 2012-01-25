package edu.ubc.mirrors;

import static edu.ubc.mirrors.MirageClassGenerator.fieldMapMirrorType;
import static edu.ubc.mirrors.MirageClassGenerator.getMirageInternalClassName;
import static edu.ubc.mirrors.MirageClassGenerator.mirageClassLoaderType;
import static edu.ubc.mirrors.MirageClassGenerator.nativeObjectMirrorType;
import static edu.ubc.mirrors.MirageClassGenerator.objectMirageType;
import static edu.ubc.mirrors.MirageClassGenerator.objectMirrorType;
import static edu.ubc.mirrors.NativeClassGenerator.getNativeInternalClassName;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;

public class MirageMethodGenerator extends InstructionAdapter {

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
            
            if (owner.equals(objectMirageType.getInternalName()) || !MirageClassLoader.COMMON_CLASSES.containsKey(owner.replace('/', '.'))) {
                desc = MirageClassGenerator.addMirrorArgToDesc(desc);
                
                if (owner.equals(superName)) {
                    // If we're calling super(...), just push the extra mirror argument on the stack
                    // TODO-RS: What if a subclass constructs a superclass instance in its constructor???
                    load(methodType.getArgumentTypes().length, objectMirrorType);
                } else {
                    // Otherwise construct it
                    anew(fieldMapMirrorType);
                    dup();
                    aconst(Type.getObjectType(owner));
                    invokestatic(objectMirageType.getInternalName(),
                                 "getNativeClass",
                                 Type.getMethodDescriptor(Type.getType(Class.class), Type.getType(Class.class)));
                    invokespecial(fieldMapMirrorType.getInternalName(), 
                                  "<init>", 
                                  Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Class.class)));
                }
            }
        }
            
        super.visitMethodInsn(opcode, owner, name, desc);
    }
    
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        Type fieldType = Type.getType(desc);
        Type fieldTypeForMirrorCall = fieldType;
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
                fieldTypeForMirrorCall = OBJECT_TYPE;
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
            super.visitFieldInsn(opcode, owner, name, desc);
        } else {
            // Pop the original argument
            int setValueLocal = lvs.newLocal(fieldType);
            if (isSet) {
                store(setValueLocal, fieldType);
            }
            
            // Push the mirror instance onto the stack
            getfield(Type.getInternalName(ObjectMirage.class), 
                     "mirror", 
                     MirageClassGenerator.objectMirrorType.getDescriptor());
            
            // Get the field mirror onto the stack
            aconst(name);
            invokeinterface(MirageClassGenerator.objectMirrorType.getInternalName(), 
                            isStatic ? "getStaticField" : "getMemberField", 
                            Type.getMethodDescriptor(MirageClassGenerator.fieldMirrorType, MirageClassGenerator.getMirageType(Type.getType(String.class))));
            
            // Call the appropriate getter/setter method on the mirror
            String methodDesc;
            if (isSet) {
                load(setValueLocal, fieldType);
                methodDesc = Type.getMethodDescriptor(Type.VOID_TYPE, fieldTypeForMirrorCall);
            } else {
                methodDesc = Type.getMethodDescriptor(fieldTypeForMirrorCall);
            }
            invokeinterface(MirageClassGenerator.fieldMirrorType.getInternalName(), 
                            (isSet ? "set" : "get") + suffix, 
                            methodDesc);
            if (!isSet && fieldTypeForMirrorCall.equals(OBJECT_TYPE)) {
                checkcast(fieldType);
            }
        }
    }
    
    @Override
    public void visitInsn(int opcode) {
        if (isToString && opcode == Opcodes.ARETURN) {
            invokestatic(MirageClassGenerator.objectMirageType.getInternalName(),
                         "getRealStringForMirage",
                         Type.getMethodDescriptor(Type.getType(String.class), Type.getType(ObjectMirage.class)));
        }
        
        super.visitInsn(opcode);
    }
    
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        // TODO calculate this more precisely (or get ASM to do it for me)
        super.visitMaxs(maxStack + 20, maxLocals + 20);
    }
}
