package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.arrayMirrorType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.classMirrorType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.classType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageInternalClassName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getOriginalInternalClassName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getPrimitiveArrayMirrorInternalName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getSortName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.instanceMirageType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.instanceMirrorType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.mirageType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.objectMirageType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.objectMirrorType;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.stringType;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;

public class MirageMethodGenerator extends InstructionAdapter {

    static String activeMethod = null;
    
    private AnalyzerAdapter analyzer;
    private LocalVariablesSorter lvs;
    private Type methodType;
    private Type owner;
    private String name;
    
    private final boolean isToString;
    private final boolean isGetStackTrace;
    
    public MirageMethodGenerator(String owner, int access, String name, String desc, MethodVisitor superVisitor, boolean isToString, boolean isGetStackTrace) {
        super(Opcodes.ASM4, null);
        this.analyzer = new AnalyzerAdapter(owner, access, name, desc, superVisitor);
        this.mv = analyzer;
        this.name = name;
        this.owner = Type.getObjectType(owner);
        this.methodType = Type.getMethodType(desc);
        this.isToString = isToString;
        this.isGetStackTrace = isGetStackTrace;
        
        activeMethod = name + desc;
    }

    public void getClassMirror(Type type) {
	if (type.getSort() == Type.OBJECT && type.getInternalName().startsWith("mirage")) {
	    getstatic(type.getInternalName(), "classMirror", classMirrorType.getDescriptor());
	} else {
	    getstatic(owner.getInternalName(), "classMirror", classMirrorType.getDescriptor());
	    aconst(type.getDescriptor());
	    new MethodHandle() {
        	protected void methodCall() throws Throwable {
        	    ObjectMirage.getClassMirrorForType(null, null);
        	}
            }.invoke(this);
	}
    }
    
    @Override
    public void visitEnd() {
        super.visitEnd();
        
        activeMethod = null;
    }
    
    public void setLocalVariablesSorter(LocalVariablesSorter lvs) {
        this.lvs = lvs;
    }
    
    public void setAnalyzer(AnalyzerAdapter analyzer) {
        this.analyzer = analyzer;
    }
    
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        Type stringMirageType = getMirageType(String.class);
        if (name.equals("toString") && desc.equals(Type.getMethodDescriptor(stringMirageType))) {
            desc = Type.getMethodDescriptor(Type.getType(String.class));
            if (owner.equals(Type.getInternalName(Mirage.class))) {
                owner = OBJECT_TYPE.getInternalName();
                // Handle calling Object.toString() with an invokespecial opcode, 
                // which doesn't work any more since we've changed the superclass.
                if (opcode == Opcodes.INVOKESPECIAL) {
                    opcode = Opcodes.INVOKESTATIC;
                    owner = objectMirageType.getInternalName();
                    name = "mirageToString";
                    desc = Type.getMethodDescriptor(stringType, mirageType);
                }
            }
            
            super.visitMethodInsn(opcode, owner, name, desc);
            
            getClassMirror(this.owner);
            invokestatic(objectMirageType.getInternalName(),
                         "makeStringMirage",
                         Type.getMethodDescriptor(mirageType, stringType, classMirrorType));
            checkcast(stringMirageType);
            return;
        }
        
        if (name.equals("getClass") && desc.equals(Type.getMethodDescriptor(getMirageType(Class.class)))) {
            invokeinterface(Type.getInternalName(Mirage.class), 
                    "getMirror", 
                    Type.getMethodDescriptor(objectMirrorType));
            invokeinterface(objectMirrorType.getInternalName(),
                            "getClassMirror",
                            Type.getMethodDescriptor(Type.getType(ClassMirror.class)));
            
            invokestatic(objectMirageType.getInternalName(),
                         "make",
                         Type.getMethodDescriptor(mirageType, objectMirrorType));
            checkcast(getMirageType(Class.class));
            return;
        }
        
        if (owner.equals(Type.getInternalName(Mirage.class))) {
            if (name.equals("<init>") && this.owner.equals(getMirageType(Throwable.class))) {
                owner = Type.getInternalName(Throwable.class);
            } else if (name.equals("<init>") || name.equals("toString")) {
                owner = objectMirageType.getInternalName();
            } else {
                owner = OBJECT_TYPE.getInternalName();
            }
        }
        
        if (name.equals("clone") && desc.equals(Type.getMethodDescriptor(mirageType))) {
            desc = Type.getMethodDescriptor(OBJECT_TYPE);
        }
        if (name.equals("clone") && owner.startsWith("miragearray") && !owner.startsWith("miragearrayimpl")) {
            String originalName = getOriginalInternalClassName(owner);
            owner = getMirageInternalClassName(originalName, true);
            checkcast(Type.getObjectType(owner));
        }
        
//        if (owner.equals(getMirageType(Throwable.class).getInternalName())) {
//            if (name.equals("<init>") && desc.equals(Type.getMethodDescriptor(Type.VOID_TYPE, getMirageType(String.class)))) {
//                desc = Type.getMethodDescriptor(Type.VOID_TYPE, objectMirageType);
//            }
//        }
        
        if (name.equals("equals") && desc.equals(Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(Mirage.class)))) {
            desc = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, OBJECT_TYPE);
        }
        
        if (name.equals("<init>") && !owner.equals(Type.getInternalName(Throwable.class))) {
            int argsSize = Type.getArgumentsAndReturnSizes(desc) >> 2;
            desc = MirageClassGenerator.addMirrorParam(desc);
            
            Object targetType = stackType(argsSize - 1);
            if (targetType.equals(Opcodes.UNINITIALIZED_THIS)) {
                // If the target is an uninitialized this (i.e. we're calling super(...)
                // or this(...)), pass along the extra mirror argument
                load((methodType.getArgumentsAndReturnSizes() >> 2) - 1, instanceMirrorType);
            } else if (targetType instanceof Label) {
                // If the target is just uninitialized (i.e. we're calling <init> after
                // a new), create the mirror
                getClassMirror(Type.getObjectType(owner));
                invokeinterface(classMirrorType.getInternalName(),
                        "newRawInstance",
                        Type.getMethodDescriptor(instanceMirrorType));
            } else {
                // Shouldn't happen
                throw new RuntimeException("Calling <init> on already initialized type: " + targetType);
            }
        }
        
        super.visitMethodInsn(opcode, owner, name, desc);
        
        if (owner.equals(Type.getInternalName(Throwable.class)) && name.equals("getStackTraceElement")) {
            Type steType = Type.getType(StackTraceElement.class);
            invokestatic(Type.getInternalName(ObjectMirage.class),
                         "cleanStackTraceElement",
                         Type.getMethodDescriptor(steType, steType));
        }
    }
    
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        boolean isSet = (opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC);
        boolean isStatic = (opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC);

        Type fieldType = Type.getType(desc);
        
        if (isStatic) {
            // For a static field the instance is null
            int setValueLocal = lvs.newLocal(fieldType);
            if (isSet) {
                store(setValueLocal, fieldType);
            }
            aconst(null);
            if (isSet) {
                load(setValueLocal, fieldType);
            }
        } else {
            // If this is an "uninitialized this", the mirror is the nth argument instead
            // of the mirror field on ObjectMirage.
            Object stackType = stackType(isSet ? 1 : 0);
            if (stackType == Opcodes.UNINITIALIZED_THIS) {
                // Pop the original argument
                int setValueLocal = lvs.newLocal(fieldType);
                if (isSet) {
                    store(setValueLocal, fieldType);
                }

                pop();
                load((methodType.getArgumentsAndReturnSizes() >> 2) - 1, instanceMirrorType);

                MethodHandle.OBJECT_MIRAGE_MAKE.invoke(this);

                if (isSet) {
                    load(setValueLocal, fieldType);
                }
            }
        }

        getClassMirror(Type.getObjectType(owner));
        aconst(name);
        
        Type fieldTypeForMirrorCall = fieldType;
        int fieldSort = fieldType.getSort();
        String suffix = "";
        if (fieldSort == Type.ARRAY || fieldSort == Type.OBJECT) {
            fieldTypeForMirrorCall = mirageType;
        } else {
            suffix = MirageClassGenerator.getSortName(fieldSort);
        }
        
        // Call the appropriate getter/setter method on the mirror
        String methodDesc;
        if (isSet) {
            methodDesc = Type.getMethodDescriptor(Type.VOID_TYPE, mirageType, fieldTypeForMirrorCall, classMirrorType, stringType);
        } else {
            methodDesc = Type.getMethodDescriptor(fieldTypeForMirrorCall, mirageType, classMirrorType, stringType);
        }
        invokestatic(instanceMirageType.getInternalName(), 
                     (isSet ? "set" : "get") + suffix + "Field", 
                     methodDesc);
        
        if (!isSet && fieldTypeForMirrorCall.equals(mirageType)) {
            checkcast(fieldType);
        }
    }
    
    private Object stackType(int indexFromTop) {
        return analyzer.stack.get(analyzer.stack.size() - 1 - indexFromTop);
    }
    
    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
        Type mirageArrayType = Type.getType(desc);
        Type originalElementType = Type.getObjectType(getOriginalInternalClassName(mirageArrayType.getInternalName())).getElementType();
        
        Type intArrayType = Type.getObjectType("[I");
        
        // Push the dimension values into an array
        int dimsArrayVar = lvs.newLocal(intArrayType);
        aconst(dims);
        newarray(Type.INT_TYPE);
        store(dimsArrayVar, intArrayType);
        
        for (int d = dims - 1; d >= 0; d--) {
            load(dimsArrayVar, intArrayType);
            swap();
            aconst(d);
            swap();
            astore(Type.INT_TYPE);
        }
        
        anew(mirageArrayType);
        dup();
        
        getClassMirror(originalElementType);
        load(dimsArrayVar, intArrayType);
        invokeinterface(classMirrorType.getInternalName(),
                "newArray",
                Type.getMethodDescriptor(Type.getType(ArrayMirror.class), intArrayType));
        
        invokespecial(mirageArrayType.getInternalName(), 
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
            case Opcodes.AALOAD: arrayElementType = mirageType; break;
            case Opcodes.BALOAD: arrayElementType = Type.BYTE_TYPE; break;
            case Opcodes.CALOAD: arrayElementType = Type.CHAR_TYPE; break;
            case Opcodes.SALOAD: arrayElementType = Type.SHORT_TYPE; break;
            case Opcodes.IASTORE: arrayElementType = Type.INT_TYPE; break;
            case Opcodes.LASTORE: arrayElementType = Type.LONG_TYPE; break;
            case Opcodes.FASTORE: arrayElementType = Type.FLOAT_TYPE; break;
            case Opcodes.DASTORE: arrayElementType = Type.DOUBLE_TYPE; break;
            case Opcodes.AASTORE: arrayElementType = mirageType; break;
            case Opcodes.BASTORE: arrayElementType = Type.BYTE_TYPE; break;
            case Opcodes.CASTORE: arrayElementType = Type.CHAR_TYPE; break;
            case Opcodes.SASTORE: arrayElementType = Type.SHORT_TYPE; break;
            }
            
            Type mirrorType = MirageClassGenerator.objectArrayMirrorType;
            if (arrayElementType.getSort() != Type.OBJECT && arrayElementType.getSort() != Type.ARRAY) {
                mirrorType = Type.getObjectType(getPrimitiveArrayMirrorInternalName(arrayElementType));
            }
            
            // Use the analyzer to figure out the expected array element type
            Type arrayElementTypeForMirrorCall = arrayElementType;
            Type mirageArrayType = Type.getObjectType((String)stackType(isArrayStore ? 1 + arrayElementType.getSize() : 1));
            if (mirageArrayType == null) {
                mirageArrayType = Type.getType(ObjectArrayMirage.class);
            }
            if (arrayElementType.equals(mirageType)) {
                Type originalType = Type.getObjectType(getOriginalInternalClassName(mirageArrayType.getInternalName()));
                arrayElementType = getMirageType(Reflection.makeArrayType(originalType.getDimensions() - 1, originalType.getElementType()));
                mirageArrayType = Type.getType(ObjectArrayMirage.class);
            }
            
            // Call the appropriate getter/setter method on the mirage
            String methodDesc;
            if (isArrayStore) {
                methodDesc = Type.getMethodDescriptor(Type.VOID_TYPE, mirrorType, Type.INT_TYPE, arrayElementTypeForMirrorCall);
            } else {
                methodDesc = Type.getMethodDescriptor(arrayElementTypeForMirrorCall, mirrorType, Type.INT_TYPE);
            }
            invokestatic(mirageArrayType.getInternalName(), 
                            (isArrayStore ? "setMirage" : "getMirage"), 
                            methodDesc);
            if (!isArrayStore && arrayElementTypeForMirrorCall.equals(mirageType)) {
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
                invokestatic(objectMirageType.getInternalName(),
                             "getRealStringForMirage",
                             Type.getMethodDescriptor(Type.getType(String.class), Type.getType(ObjectMirage.class)));
            } else if (isGetStackTrace) {
                invokestatic(objectMirageType.getInternalName(),
                             "getRealStackTraceForMirage",
                             Type.getMethodDescriptor(Type.getType(StackTraceElement[].class), Type.getType(Mirage.class)));
            }
        }
        super.visitInsn(opcode);
    }
    
    @Override
    public void visitTypeInsn(int opcode, String type) {
        if (opcode == Opcodes.ANEWARRAY) {
            String originalTypeName = getOriginalInternalClassName(type);
            Type arrayType = Reflection.makeArrayType(1, Type.getObjectType(originalTypeName));
            
            getClassMirror(Type.getObjectType(type));
            swap();
            invokeinterface(classMirrorType.getInternalName(),
                    "newArray",
                    Type.getMethodDescriptor(Type.getType(ArrayMirror.class), Type.INT_TYPE));
            
            // Instantiate the mirage class
            Type mirageArrayType = Type.getObjectType(getMirageInternalClassName(arrayType.getInternalName(), true));
            anew(mirageArrayType);
            dupX1();
            swap();
            
            invokespecial(mirageArrayType.getInternalName(), 
                          "<init>", 
                          Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(ObjectArrayMirror.class)));
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
            
            getClassMirror(elementType);
            swap();
            invokeinterface(classMirrorType.getInternalName(),
                    "newArray",
                    Type.getMethodDescriptor(Type.getType(ArrayMirror.class), Type.INT_TYPE));
            
            String arrayMirageType = "edu/ubc/mirrors/mirages/" + getSortName(elementType.getSort()) + "ArrayMirage";
            
            invokestatic(objectMirageType.getInternalName(),
                    "make",
                    Type.getMethodDescriptor(mirageType, objectMirrorType));
            checkcast(Type.getObjectType(arrayMirageType));
            
            return;
        }
        
        super.visitIntInsn(opcode, operand);
    }
    
    @Override
    public void visitLdcInsn(Object cst) {
        super.visitLdcInsn(cst);
        
        if (cst instanceof String) {
            Type mirageStringType = getMirageType(stringType);
            getClassMirror(this.owner);
            invokestatic(objectMirageType.getInternalName(),
                         "makeStringMirage",
                         Type.getMethodDescriptor(mirageType, stringType, classMirrorType));
            checkcast(mirageStringType);
        } else if (cst instanceof Type) {
            Type mirageClassType = getMirageType(classType);
            getClassMirror(this.owner);
            invokestatic(objectMirageType.getInternalName(),
                         "makeClassMirage",
                         Type.getMethodDescriptor(mirageType, classType, classMirrorType));
            checkcast(mirageClassType);
        }
    }
    
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        // TODO calculate this more precisely (or get ASM to do it for me)
        super.visitMaxs(maxStack + 20, maxLocals + 20);
    }
    
    @Override
    public void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start);
        
//        getstatic(Type.getInternalName(System.class), "out", Type.getDescriptor(PrintStream.class));
//        aconst(owner + "#" + name + ":" + line);
//        invokevirtual(Type.getInternalName(PrintStream.class), "println", Type.getMethodDescriptor(Type.VOID_TYPE, OBJECT_TYPE));
    }
    
    public static void initializeStaticFields(Type owner, InstructionAdapter mv) {
	// Initialize the static field that holds the ClassMirror for this holographic Class
        mv.aconst(owner);
        new MethodHandle() {
    	protected void methodCall() throws Throwable {
    	    ObjectMirage.getClassMirrorForHolographicClass(null);
    	}
        }.invoke(mv);
        mv.dup();
        mv.putstatic(owner.getInternalName(), "classMirror", classMirrorType.getDescriptor());
        
        // Initialize the static field that holds the native stubs instance (if any)
        String originalClassName = MirageClassGenerator.getOriginalInternalClassName(owner.getInternalName());
        Class<?> nativeStubsClass = ClassHolograph.getNativeStubsClass(originalClassName.replace('/', '.'));
        if (nativeStubsClass != null) {
            mv.dup();
            new MethodHandle() {
        	protected void methodCall() throws Throwable {
        	    ObjectMirage.getNativeStubsInstanceForClassMirror(null);
        	}
            }.invoke(mv);
            mv.checkcast(Type.getType(nativeStubsClass));
            mv.putstatic(owner.getInternalName(), "nativeStubs", Type.getDescriptor(nativeStubsClass));
        }
    }
    
    @Override
    public void visitCode() {
        super.visitCode();

        if (name.equals("<clinit>")) {
            initializeStaticFields(owner, this);
            
            // Skip the static initializer for classes that were already defined - 
            // these will have already been executed when
            // loading the original class and state will be proxied by ClassMirrors instead.
            Label afterEarlyReturn = new Label();
            new MethodHandle() {
	        protected void methodCall() throws Throwable {
	    	   ((ClassMirror)null).initialized();
	        }
	    }.invoke(this);
            ifeq(afterEarlyReturn);
            areturn(Type.VOID_TYPE);
            mark(afterEarlyReturn);
            visitFrame(Opcodes.F_NEW, 0, new Object[0], 0, new Object[0]);
        }
        
        if (name.equals("<init>")) {
            lvs.newLocal(instanceMirrorType);

            if (owner.equals(getMirageType(Throwable.class))) {
                load(0, owner);
                load((methodType.getArgumentsAndReturnSizes() >> 2) - 1, instanceMirrorType);
                putfield(owner.getInternalName(), "mirror", Type.getDescriptor(ObjectMirror.class));
            }
        }
    }
}
