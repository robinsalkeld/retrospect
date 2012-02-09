package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.arrayMirrorType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.classType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.fieldMapMirrorType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.fieldMirrorType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageInternalClassName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getOriginalClassName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getPrimitiveArrayMirrorInternalName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getSortName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.instanceMirrorType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.nativeObjectMirrorType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.objectMirageType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.objectMirrorType;
import static edu.ubc.mirrors.mirages.MirageClassLoader.CLASS_LOADER_LITERAL_NAME;
import static edu.ubc.mirrors.raw.NativeClassGenerator.getNativeInternalClassName;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;

import edu.ubc.mirrors.raw.NativeObjectArrayMirror;

public class MirageMethodGenerator extends InstructionAdapter {

    private AnalyzerAdapter analyzer;
    private LocalVariablesSorter lvs;
    private final String superName;
    private final Type methodType;
    private final boolean isToString;
    
    public MirageMethodGenerator(AnalyzerAdapter analyzer, String superName, Type methodType, boolean isToString) {
        super(Opcodes.ASM4, analyzer);
        this.analyzer = analyzer;
        this.superName = superName;
        this.methodType = methodType;
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
        if (name.equals("<init>")) {
            if (owner.equals(Type.getInternalName(Object.class))) {
                owner = objectMirageType.getInternalName();
            }
        }
            
        super.visitMethodInsn(opcode, owner, name, desc);
    }
    
    
    private void fieldMirrorInsn(boolean isSet, Type fieldType) {
        Type fieldTypeForMirrorCall = fieldType;
        int fieldSort = fieldType.getSort();
        String suffix = "";
        if (fieldSort == Type.ARRAY || fieldSort == Type.OBJECT) {
            fieldTypeForMirrorCall = OBJECT_TYPE;
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
        invokeinterface(MirageClassGenerator.fieldMirrorType.getInternalName(), 
                        (isSet ? "set" : "get") + suffix, 
                        methodDesc);
        if (!isSet && fieldTypeForMirrorCall.equals(OBJECT_TYPE)) {
            checkcast(fieldType);
        }
    }
    
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        boolean isSet = (opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC);
        boolean isStatic = (opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC);
        
        if (isStatic) {
            // TODO-RS: See comment in MirageClassGenerator#visitMethod()
            super.visitFieldInsn(opcode, owner, name, desc);
            return;
        }
        
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
                     instanceMirrorType.getDescriptor());
            
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
            // TODO-RS: Sometimes the analyzer doesn't know the stack types :(
            return Type.getType("[Ljava/lang/Object;");
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
            case Opcodes.AALOAD: arrayElementType = OBJECT_TYPE; break;
            case Opcodes.BALOAD: arrayElementType = Type.BYTE_TYPE; break;
            case Opcodes.CALOAD: arrayElementType = Type.CHAR_TYPE; break;
            case Opcodes.SALOAD: arrayElementType = Type.SHORT_TYPE; break;
            case Opcodes.IASTORE: arrayElementType = Type.INT_TYPE; break;
            case Opcodes.LASTORE: arrayElementType = Type.LONG_TYPE; break;
            case Opcodes.FASTORE: arrayElementType = Type.FLOAT_TYPE; break;
            case Opcodes.DASTORE: arrayElementType = Type.DOUBLE_TYPE; break;
            case Opcodes.AASTORE: arrayElementType = OBJECT_TYPE; break;
            case Opcodes.BASTORE: arrayElementType = Type.BYTE_TYPE; break;
            case Opcodes.CASTORE: arrayElementType = Type.CHAR_TYPE; break;
            case Opcodes.SASTORE: arrayElementType = Type.SHORT_TYPE; break;
            }
            
            // Use the analyzer to figure out the expected array element type
            Type arrayElementTypeForMirrorCall = arrayElementType;
            if (arrayElementType.equals(OBJECT_TYPE)) {
                Type mirageType = stackType(isArrayStore ? 2 : 1);
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
            // TODO-RS: Primitive mirrors don't need to be wrapped?
            Type mirrorType = MirageClassGenerator.arrayMirrorType;
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
            invokeinterface(mirrorType.getInternalName(), 
                            (isArrayStore ? "set" : "get") + suffix, 
                            methodDesc);
            if (!isArrayStore && arrayElementTypeForMirrorCall.equals(OBJECT_TYPE)) {
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
            super.visitTypeInsn(opcode, type);
            
            invokespecial(mirageArrayType, 
                          "<init>", 
                          Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object[].class)));
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
            case Opcodes.T_LONG:    elementType = Type.SHORT_TYPE;   break;
            case Opcodes.T_FLOAT:   elementType = Type.FLOAT_TYPE;   break;
            case Opcodes.T_DOUBLE:  elementType = Type.DOUBLE_TYPE;  break;
            default: throw new IllegalArgumentException("Unknown type number: " + operand);
            }
            String arrayMirrorType = "edu/ubc/mirrors/raw/Native" + getSortName(elementType.getSort()) + "ArrayMirror";
            anew(Type.getObjectType(arrayMirrorType));
            dup();

            load(arraySizeVar, Type.INT_TYPE);
            super.visitIntInsn(opcode, operand);
            
            invokespecial(arrayMirrorType, 
                          "<init>", 
                          Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType("[" + elementType.getDescriptor())));
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
                         "getMirageStringForReal", 
                         Type.getMethodDescriptor(OBJECT_TYPE, Type.getType(String.class), Type.getType(Class.class)));
            checkcast(mirageStringType);
        }
    }
    
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        // TODO calculate this more precisely (or get ASM to do it for me)
        super.visitMaxs(maxStack + 20, maxLocals + 20);
    }
}
