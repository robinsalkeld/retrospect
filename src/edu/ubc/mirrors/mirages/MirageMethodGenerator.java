package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.arrayMirrorType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.classType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.fieldMirrorType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageInternalClassName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getOriginalInternalClassName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getPrimitiveArrayMirageInternalName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getPrimitiveArrayMirrorInternalName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getSortName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.instanceMirrorType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.makeArrayType;
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

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;
import edu.ubc.mirrors.raw.NativeObjectArrayMirror;

public class MirageMethodGenerator extends InstructionAdapter {

    private AnalyzerAdapter analyzer;
    private MethodVisitor superVisitor;
    private LocalVariablesSorter lvs;
    private Type methodType;
    private String owner;
    private String name;
    
    private final boolean isToString;
    
    public MirageMethodGenerator(String owner, int access, String name, String desc, MethodVisitor superVisitor, boolean isToString) {
        super(Opcodes.ASM4, null);
        this.superVisitor = superVisitor;
        this.analyzer = new AnalyzerAdapter(owner, access, name, desc, superVisitor);
        this.mv = analyzer;
        this.name = name;
        this.owner = owner;
        this.methodType = Type.getMethodType(desc);
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
            
            invokestatic(CLASS_LOADER_LITERAL_NAME,
                         "lift",
                         Type.getMethodDescriptor(OBJECT_TYPE, OBJECT_TYPE));
            checkcast(getMirageType(String.class));
            return;
        }
        
        if (name.equals("getClass") && desc.equals(Type.getMethodDescriptor(getMirageType(Class.class)))) {
            invokeinterface(Type.getInternalName(Mirage.class), 
                    "getMirror", 
                    Type.getMethodDescriptor(objectMirrorType));
            invokeinterface(objectMirrorType.getInternalName(),
                            "getClassMirror",
                            Type.getMethodDescriptor(Type.getType(ClassMirror.class)));
            
            invokestatic(CLASS_LOADER_LITERAL_NAME,
                         "makeMirage",
                         Type.getMethodDescriptor(OBJECT_TYPE, objectMirrorType));
            checkcast(getMirageType(Class.class));
            return;
        }
        
        if (owner.equals(Type.getInternalName(Mirage.class))) {
            if (name.equals("<init>")) {
                owner = objectMirageType.getInternalName();
            } else {
                owner = OBJECT_TYPE.getInternalName();
            }
        }
        
        if (name.equals("<init>")) {
            int argsSize = Type.getArgumentsAndReturnSizes(desc) >> 2;
            desc = MirageClassGenerator.addMirrorParam(desc);
            
            Object targetType = stackType(argsSize - 1);
            if (targetType.equals(Opcodes.UNINITIALIZED_THIS)) {
                // If the target is an uninitialized this (i.e. we're calling super(...)
                // of this(...)), pass along the extra mirror argument
                load((methodType.getArgumentsAndReturnSizes() >> 2) - 1, instanceMirrorType);
            } else if (targetType instanceof Label) {
                // If the target is just uninitialized (i.e. we're calling <init> after
                // a new), create the mirror
                aconst(owner);
                invokestatic(CLASS_LOADER_LITERAL_NAME,
                        "newInstanceMirror",
                        Type.getMethodDescriptor(instanceMirrorType, Type.getType(String.class)));
            } else {
                // Shouldn't happen
                throw new InternalError();
            }
        }
        
        super.visitMethodInsn(opcode, owner, name, desc);
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
            invokestatic(CLASS_LOADER_LITERAL_NAME,
                         "makeMirage",
                         Type.getMethodDescriptor(OBJECT_TYPE, objectMirrorType));
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
            aconst(MirageClassGenerator.getOriginalInternalClassName(owner));
            aconst(name);
            invokestatic(CLASS_LOADER_LITERAL_NAME, 
                         "getStaticField", 
                         Type.getMethodDescriptor(fieldMirrorType, Type.getType(String.class), Type.getType(String.class)));
        } else {
            // Push the mirror instance onto the stack
            // If this is an "uninitialized this", the mirror is the nth argument instead
            // of the mirror field on ObjectMirage.
            Object stackType = stackType(0);
            if (stackType == Opcodes.UNINITIALIZED_THIS) {
                load(methodType.getArgumentTypes().length, instanceMirrorType);
            } else {
                invokeinterface(Type.getInternalName(Mirage.class), 
                                "getMirror", 
                                Type.getMethodDescriptor(objectMirrorType));
                checkcast(instanceMirrorType);
            }
            
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
    
    private Object stackType(int indexFromTop) {
        return analyzer.stack.get(analyzer.stack.size() - 1 - indexFromTop);
    }
    
    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
        String internalArrayName = Type.getType(desc).getInternalName();
        Type originalArrayType = Type.getObjectType(getOriginalInternalClassName(internalArrayName));
        super.visitMultiANewArrayInsn(originalArrayType.getInternalName(), dims);
        
        int arrayLocal = lvs.newLocal(originalArrayType);
        store(arrayLocal, originalArrayType);
        
        anew(Type.getType(desc));
        dup();
        
        anew(Type.getType(NativeObjectArrayMirror.class));
        dup();
        
        load(arrayLocal, originalArrayType);
        
        invokespecial(Type.getInternalName(NativeObjectArrayMirror.class), 
                      "<init>", 
                      Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object[].class)));
        invokespecial(internalArrayName, 
                      "<init>", 
                      Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(ObjectArrayMirror.class)));
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
                Type mirageType = Type.getObjectType((String)stackType(isArrayStore ? 2 : 1));
                if (mirageType == null) {
                    mirageType = Type.getType(ObjectArrayMirage.class);
                }
                Type originalType = Type.getObjectType(getOriginalInternalClassName(mirageType.getInternalName()));
                arrayElementType = getMirageType(makeArrayType(originalType.getDimensions() - 1, originalType.getElementType()));
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
            
            invokestatic(objectMirageType.getInternalName(), 
                    "getMirror", 
                    Type.getMethodDescriptor(objectMirrorType, OBJECT_TYPE));
            checkcast(mirrorType);
            
            load(indexLocal, Type.INT_TYPE);
            if (isArrayStore) {
                load(setValueLocal, arrayElementType);
                if (arrayElementTypeForMirrorCall.equals(objectMirrorType)) {
                    invokestatic(objectMirageType.getInternalName(), 
                                 "getMirror", 
                                 Type.getMethodDescriptor(objectMirrorType, OBJECT_TYPE));
                }
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
            if (!isArrayStore && arrayElementTypeForMirrorCall.equals(objectMirrorType)) {
                invokestatic(CLASS_LOADER_LITERAL_NAME,
                        "makeMirage",
                        Type.getMethodDescriptor(OBJECT_TYPE, objectMirrorType));
                checkcast(arrayElementType);
            }
            
            return;
        }
        
        if (opcode == Opcodes.ARRAYLENGTH) {
            invokeinterface(arrayMirrorType.getInternalName(), "length", Type.getMethodDescriptor(Type.INT_TYPE));
            return;
        }
        
        if (opcode == Opcodes.ARETURN) {
            if (isToString) {
                invokestatic(MirageClassGenerator.objectMirageType.getInternalName(),
                             "getRealStringForMirage",
                             Type.getMethodDescriptor(Type.getType(String.class), Type.getType(ObjectMirage.class)));
            } else if (analyzer.stack == null) {
                checkcast(methodType.getReturnType());
            }
        }
        super.visitInsn(opcode);
    }
    
    @Override
    public void jsr(Label label) {
        // Don't tell the analyzer - it doesn't support them!
        superVisitor.visitJumpInsn(Opcodes.JSR, label);
    }
    
    @Override
    public void ret(int var) {
     // Don't tell the analyzer - it doesn't support them!
        superVisitor.visitVarInsn(Opcodes.RET, var);
    }
    
    @Override
    public void visitTypeInsn(int opcode, String type) {
        if (opcode == Opcodes.ANEWARRAY) {
            // Store the array size
            int arraySizeVar = lvs.newLocal(Type.INT_TYPE);
            store(arraySizeVar, Type.INT_TYPE);
            
            // Instantiate the mirage class
            String originalTypeName = getOriginalInternalClassName(type);
            Type arrayType = makeArrayType(1, Type.getObjectType(originalTypeName));
            Type mirageArrayType = Type.getObjectType(getMirageInternalClassName(arrayType.getInternalName(), true));
            anew(mirageArrayType);
            dup();

            load(arraySizeVar, Type.INT_TYPE);
            invokespecial(mirageArrayType.getInternalName(), 
                          "<init>", 
                          Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE));
            return;
        }
        
        if (opcode == Opcodes.NEW && type.equals(Type.getInternalName(Mirage.class))) {
            type = objectMirageType.getInternalName();
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
            
            invokestatic(CLASS_LOADER_LITERAL_NAME,
                    "makeMirage",
                    Type.getMethodDescriptor(OBJECT_TYPE, objectMirrorType));
            checkcast(Type.getObjectType(arrayMirageType));
            
            return;
        }
        
        super.visitIntInsn(opcode, operand);
    }
    
    @Override
    public void visitLdcInsn(Object cst) {
        super.visitLdcInsn(cst);
        
        if (cst instanceof String) {
            Type mirageStringType = getMirageType(Type.getType(String.class));
            invokestatic(CLASS_LOADER_LITERAL_NAME, 
                         "lift", 
                         Type.getMethodDescriptor(OBJECT_TYPE, OBJECT_TYPE));
            checkcast(mirageStringType);
        } else if (cst instanceof Type) {
            Type mirageClassType = getMirageType(Type.getType(Class.class));
            invokestatic(CLASS_LOADER_LITERAL_NAME, 
                         "lift", 
                         Type.getMethodDescriptor(OBJECT_TYPE, OBJECT_TYPE));
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
    
    @Override
    public void visitCode() {
        super.visitCode();
        
        if (name.equals("<init>")) {
            lvs.newLocal(instanceMirrorType);
        }
    }
}
