package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.arrayMirrorType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.classType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.fieldMirrorType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageInternalClassName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getOriginalClassName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getPrimitiveArrayMirageInternalName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getPrimitiveArrayMirrorInternalName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getSortName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.instanceMirrorType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.objectMirageType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.objectMirrorType;
import static edu.ubc.mirrors.mirages.MirageClassLoader.CLASS_LOADER_LITERAL_NAME;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;

public class MirageMethodGenerator extends InstructionAdapter {

    private AnalyzerAdapter analyzer;
    private MethodVisitor superVisitor;
    private LocalVariablesSorter lvs;
    
    private final boolean isToString;
    
    public MirageMethodGenerator(String owner, int access, String name, String desc, MethodVisitor superVisitor, boolean isToString) {
        super(Opcodes.ASM4, null);
        this.superVisitor = superVisitor;
        this.analyzer = new AnalyzerAdapter(owner, access, name, desc, superVisitor);
        this.mv = analyzer;
        this.isToString = isToString;
    }

    public void setLocalVariablesSorter(LocalVariablesSorter lvs) {
        this.lvs = lvs;
    }
    
    public void setAnalyzer(AnalyzerAdapter analyzer) {
        this.analyzer = analyzer;
    }
    
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (name.equals("toString") && desc.equals(Type.getMethodDescriptor(getMirageType(String.class)))) {
            super.visitMethodInsn(opcode, OBJECT_TYPE.getInternalName(), name, Type.getMethodDescriptor(Type.getType(String.class)));
            
            aconst(Type.getObjectType(CLASS_LOADER_LITERAL_NAME));
            invokestatic(objectMirageType.getInternalName(),
                         "lift",
                         Type.getMethodDescriptor(OBJECT_TYPE, OBJECT_TYPE, classType));
            checkcast(getMirageType(String.class));
            return;
        }
        
        if (analyzer.locals == null) {
            insertDowncastsForMethodCall(opcode, owner, name, desc);
        }
        
        super.visitMethodInsn(opcode, owner, name, desc);
    }
    
    
    private void insertDowncastsForMethodCall(int opcode, String owner, String name, String desc) {
        Type[] argTypes = Type.getMethodType(desc).getArgumentTypes();
        int[] argLocals = new int[argTypes.length];
        for (int argIndex = argTypes.length - 1; argIndex >= 0; argIndex--) {
            Type argType = argTypes[argIndex];
            if (MirageClassGenerator.isRefType(argType)) {
                checkcast(argType);
            }
            
            int argLocal = lvs.newLocal(argType);
            argLocals[argIndex] = argLocal;
            store(argLocal, argType);
        }
        if (opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKEDYNAMIC) {
            Type ownerType = Type.getObjectType(owner);
            checkcast(ownerType);
        }
        for (int argIndex = 0; argIndex < argTypes.length; argIndex++) {
            load(argLocals[argIndex], argTypes[argIndex]);
        }
    }

    private void fieldMirrorInsn(boolean isSet, Type fieldType) {
        Type fieldTypeForMirrorCall = fieldType;
        int fieldSort = fieldType.getSort();
        String suffix = "";
        if (fieldSort == Type.ARRAY || fieldSort == Type.OBJECT) {
            fieldTypeForMirrorCall = objectMirrorType;
        } else {
            suffix = MirageClassGenerator.getSortName(fieldSort);
        }
        
        // Call the appropriate getter/setter method on the mirror
        String methodDesc;
        if (isSet) {
            methodDesc = Type.getMethodDescriptor(Type.VOID_TYPE, fieldTypeForMirrorCall);
        } else {
            methodDesc = Type.getMethodDescriptor(fieldTypeForMirrorCall);
        }
        if (isSet && fieldTypeForMirrorCall.equals(objectMirrorType)) {
            invokestatic(objectMirageType.getInternalName(), 
                         "getMirror", 
                         Type.getMethodDescriptor(objectMirrorType, OBJECT_TYPE));
        }
        invokeinterface(MirageClassGenerator.fieldMirrorType.getInternalName(), 
                        (isSet ? "set" : "get") + suffix, 
                        methodDesc);
        
        if (!isSet && fieldTypeForMirrorCall.equals(objectMirrorType)) {
            aconst(Type.getObjectType(CLASS_LOADER_LITERAL_NAME));
            invokestatic(objectMirageType.getInternalName(),
                         "make",
                         Type.getMethodDescriptor(OBJECT_TYPE, objectMirrorType, classType));
            checkcast(fieldType);
        }
    }
    
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        boolean isSet = (opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC);
        boolean isStatic = (opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC);
        
        // Pop the original argument
        Type fieldType = Type.getType(desc);
        int setValueLocal = lvs.newLocal(fieldType);
        if (isSet) {
            store(setValueLocal, fieldType);
        }
        
        // Get the field mirror onto the stack
        if (isStatic) {
            // Get the class mirror onto the stack
            aconst(Type.getObjectType(CLASS_LOADER_LITERAL_NAME));
            aconst(MirageClassGenerator.getOriginalClassName(owner));
            aconst(name);
            invokestatic(objectMirageType.getInternalName(), 
                         "getStaticField", 
                         Type.getMethodDescriptor(fieldMirrorType, classType, Type.getType(String.class), Type.getType(String.class)));
        } else {
            // Push the mirror instance onto the stack
            getfield(Type.getInternalName(ObjectMirage.class), 
                     "mirror", 
                     objectMirrorType.getDescriptor());
            checkcast(instanceMirrorType);
            
            // Get the field mirror onto the stack
            aconst(name);
            invokeinterface(instanceMirrorType.getInternalName(), 
                            "getMemberField", 
                            Type.getMethodDescriptor(fieldMirrorType, Type.getType(String.class)));
        }
        
        if (isSet) {
            load(setValueLocal, fieldType);
        }
        
        fieldMirrorInsn(isSet, fieldType);
    }
    
    private Type stackType(int indexFromTop) {
        if (analyzer.stack == null) {
            // If we're analyzing < 1.6 bytecode and we've hit a branching instruction,
            // we don't know the stack and local types. Instead we'll make no assumptions
            // about the value and insert downcasts as needed later on to ensure valid code.
            return null;
        } else {
            return Type.getObjectType((String)analyzer.stack.get(analyzer.stack.size() - 1 - indexFromTop));
        }
    }
    
    @Override
    public void visitInsn(int opcode) {
        Type arrayElementType = null;
        boolean isArrayLoad = (Opcodes.IALOAD <= opcode && opcode < Opcodes.IALOAD + 8);
        boolean isArrayStore = (Opcodes.IASTORE <= opcode && opcode < Opcodes.IASTORE + 8);
        if (isArrayLoad || isArrayStore) {
            switch (opcode) {
            case Opcodes.IALOAD: arrayElementType = Type.INT_TYPE; break;
            case Opcodes.LALOAD: arrayElementType = Type.LONG_TYPE; break;
            case Opcodes.FALOAD: arrayElementType = Type.FLOAT_TYPE; break;
            case Opcodes.DALOAD: arrayElementType = Type.DOUBLE_TYPE; break;
            case Opcodes.AALOAD: arrayElementType = objectMirrorType; break;
            case Opcodes.BALOAD: arrayElementType = Type.BYTE_TYPE; break;
            case Opcodes.CALOAD: arrayElementType = Type.CHAR_TYPE; break;
            case Opcodes.SALOAD: arrayElementType = Type.SHORT_TYPE; break;
            case Opcodes.IASTORE: arrayElementType = Type.INT_TYPE; break;
            case Opcodes.LASTORE: arrayElementType = Type.LONG_TYPE; break;
            case Opcodes.FASTORE: arrayElementType = Type.FLOAT_TYPE; break;
            case Opcodes.DASTORE: arrayElementType = Type.DOUBLE_TYPE; break;
            case Opcodes.AASTORE: arrayElementType = objectMirrorType; break;
            case Opcodes.BASTORE: arrayElementType = Type.BYTE_TYPE; break;
            case Opcodes.CASTORE: arrayElementType = Type.CHAR_TYPE; break;
            case Opcodes.SASTORE: arrayElementType = Type.SHORT_TYPE; break;
            }
            
            // Use the analyzer to figure out the expected array element type
            Type arrayElementTypeForMirrorCall = arrayElementType;
            if (arrayElementType.equals(objectMirrorType)) {
                Type mirageType = stackType(isArrayStore ? 2 : 1);
                if (mirageType == null) {
                    mirageType = Type.getType("[Ljava/lang/Object;");
                }
                Type originalType = Type.getObjectType(getOriginalClassName(mirageType.getInternalName()));
                arrayElementType = getMirageType(originalType.getElementType());
            }
            
            int setValueLocal = -1; 
            if (isArrayStore) {
                setValueLocal = lvs.newLocal(arrayElementType);
                store(setValueLocal, arrayElementType);
            }
            int indexLocal = lvs.newLocal(Type.INT_TYPE);
            store(indexLocal, Type.INT_TYPE);
            
            // Push the mirror instance onto the stack
            Type mirrorType = MirageClassGenerator.objectArrayMirrorType;
            String suffix = "";
            if (arrayElementType.getSort() != Type.OBJECT && arrayElementType.getSort() != Type.ARRAY) {
                mirrorType = Type.getObjectType(getPrimitiveArrayMirrorInternalName(arrayElementType));
                suffix = getSortName(arrayElementType.getSort());
            }
            
            load(indexLocal, Type.INT_TYPE);
            if (isArrayStore) {
                load(setValueLocal, arrayElementType);
            }
            
            // Call the appropriate getter/setter method on the mirror
            String methodDesc;
            if (isArrayStore) {
                methodDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, arrayElementTypeForMirrorCall);
            } else {
                methodDesc = Type.getMethodDescriptor(arrayElementTypeForMirrorCall, Type.INT_TYPE);
            }
            if (isArrayStore && arrayElementTypeForMirrorCall.equals(objectMirrorType)) {
                invokestatic(objectMirageType.getInternalName(), 
                             "getMirror", 
                             Type.getMethodDescriptor(objectMirrorType, OBJECT_TYPE));
            }
            invokeinterface(mirrorType.getInternalName(), 
                            (isArrayStore ? "set" : "get") + suffix, 
                            methodDesc);
            if (!isArrayStore && arrayElementTypeForMirrorCall.equals(objectMirrorType)) {
                aconst(Type.getObjectType(CLASS_LOADER_LITERAL_NAME));
                invokestatic(objectMirageType.getInternalName(),
                             "make",
                             Type.getMethodDescriptor(OBJECT_TYPE, objectMirrorType, classType));
                checkcast(arrayElementType);
            }
            
            return;
        }
        
        if (opcode == Opcodes.ARRAYLENGTH) {
            invokeinterface(arrayMirrorType.getInternalName(), "length", Type.getMethodDescriptor(Type.INT_TYPE));
            return;
        }
        
        if (isToString && opcode == Opcodes.ARETURN) {
            invokestatic(MirageClassGenerator.objectMirageType.getInternalName(),
                         "getRealStringForMirage",
                         Type.getMethodDescriptor(Type.getType(String.class), Type.getType(ObjectMirage.class)));
        }
        
        super.visitInsn(opcode);
    }
    
    @Override
    public void visitJumpInsn(int opcode, Label label) {
        if (opcode == Opcodes.JSR || opcode == Opcodes.RET) {
            // Don't tell the analyzer - it doesn't support them!
            superVisitor.visitJumpInsn(opcode, label);
        } else {
            super.visitJumpInsn(opcode, label);
        }
    }
    
    @Override
    public void visitTypeInsn(int opcode, String type) {
        if (opcode == Opcodes.ANEWARRAY) {
            // Store the array size
            int arraySizeVar = lvs.newLocal(Type.INT_TYPE);
            store(arraySizeVar, Type.INT_TYPE);
            
            // Wrap with a mirage class
            String originalTypeName = getOriginalClassName(type);
            String mirageArrayType = getMirageInternalClassName("[L" + originalTypeName + ";");
            anew(Type.getType(mirageArrayType));
            dup();

            load(arraySizeVar, Type.INT_TYPE);
            invokespecial(mirageArrayType, 
                          "<init>", 
                          Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE));
            return;
        }
        
        super.visitTypeInsn(opcode, type);
        
    }
    
    @Override
    public void visitIntInsn(int opcode, int operand) {
        if (opcode == Opcodes.NEWARRAY) {
            // Store the array size
            int arraySizeVar = lvs.newLocal(Type.INT_TYPE);
            store(arraySizeVar, Type.INT_TYPE);
            
            // Wrap with an ArrayMirror
            Type elementType;
            switch (operand) {
            case Opcodes.T_BOOLEAN: elementType = Type.BOOLEAN_TYPE; break;
            case Opcodes.T_BYTE:    elementType = Type.BYTE_TYPE;    break;
            case Opcodes.T_CHAR:    elementType = Type.CHAR_TYPE;    break;
            case Opcodes.T_SHORT:   elementType = Type.SHORT_TYPE;   break;
            case Opcodes.T_INT:     elementType = Type.INT_TYPE;     break;
            case Opcodes.T_LONG:    elementType = Type.LONG_TYPE;   break;
            case Opcodes.T_FLOAT:   elementType = Type.FLOAT_TYPE;   break;
            case Opcodes.T_DOUBLE:  elementType = Type.DOUBLE_TYPE;  break;
            default: throw new IllegalArgumentException("Unknown type number: " + operand);
            }
            String nativeArrayMirrorType = "edu/ubc/mirrors/raw/Native" + getSortName(elementType.getSort()) + "ArrayMirror";
            String arrayMirageType = "edu/ubc/mirrors/mirages/" + getSortName(elementType.getSort()) + "ArrayMirage";
            anew(Type.getObjectType(nativeArrayMirrorType));
            dup();

            load(arraySizeVar, Type.INT_TYPE);
            super.visitIntInsn(opcode, operand);
            
            invokespecial(nativeArrayMirrorType, 
                          "<init>", 
                          Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType("[" + elementType.getDescriptor())));
            
            aconst(Type.getObjectType(CLASS_LOADER_LITERAL_NAME));
            invokestatic(objectMirageType.getInternalName(),
                         "make",
                         Type.getMethodDescriptor(OBJECT_TYPE, objectMirrorType, classType));
            checkcast(Type.getObjectType(arrayMirageType));
            
            return;
        }
        
        super.visitIntInsn(opcode, operand);
    }
    
    @Override
    public void visitLdcInsn(Object cst) {
        super.visitLdcInsn(cst);
        
        if (cst instanceof String) {
            aconst(Type.getObjectType(CLASS_LOADER_LITERAL_NAME));
            Type mirageStringType = getMirageType(Type.getType(String.class));
            invokestatic(objectMirageType.getInternalName(), 
                         "lift", 
                         Type.getMethodDescriptor(OBJECT_TYPE, OBJECT_TYPE, Type.getType(Class.class)));
            checkcast(mirageStringType);
        } else if (cst instanceof Type) {
            aconst(Type.getObjectType(CLASS_LOADER_LITERAL_NAME));
            Type mirageClassType = getMirageType(Type.getType(Class.class));
            invokestatic(objectMirageType.getInternalName(), 
                         "lift", 
                         Type.getMethodDescriptor(OBJECT_TYPE, OBJECT_TYPE, Type.getType(Class.class)));
            checkcast(mirageClassType);
        }
    }
    
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        // TODO calculate this more precisely (or get ASM to do it for me)
        super.visitMaxs(maxStack + 20, maxLocals + 20);
    }
    
    @Override
    public void visitAttribute(Attribute attr) {
        // Do nothing?
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return null;
    }
}
