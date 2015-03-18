/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package edu.ubc.mirrors.holograms;

import static edu.ubc.mirrors.holograms.HologramClassGenerator.arrayMirrorType;
import static edu.ubc.mirrors.holograms.HologramClassGenerator.classMirrorType;
import static edu.ubc.mirrors.holograms.HologramClassGenerator.classType;
import static edu.ubc.mirrors.holograms.HologramClassGenerator.getHologramType;
import static edu.ubc.mirrors.holograms.HologramClassGenerator.getOriginalInternalClassName;
import static edu.ubc.mirrors.holograms.HologramClassGenerator.getPrimitiveArrayMirrorType;
import static edu.ubc.mirrors.holograms.HologramClassGenerator.getSortName;
import static edu.ubc.mirrors.holograms.HologramClassGenerator.hologramType;
import static edu.ubc.mirrors.holograms.HologramClassGenerator.instanceHologramType;
import static edu.ubc.mirrors.holograms.HologramClassGenerator.instanceMirrorType;
import static edu.ubc.mirrors.holograms.HologramClassGenerator.objectHologramType;
import static edu.ubc.mirrors.holograms.HologramClassGenerator.throwableType;
import static edu.ubc.mirrors.holograms.HologramClassGenerator.hologramThrowableType;
import static edu.ubc.mirrors.holograms.HologramClassGenerator.objectMirrorType;
import static edu.ubc.mirrors.holograms.HologramClassGenerator.stringType;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodHandle;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;

public class HologramMethodGenerator extends InstructionAdapter {

    static String activeMethod = null;
    
    private AnalyzerAdapter analyzer;
    private LocalVariablesSorter lvs;
    private Type methodType;
    private Type owner;
    private String name;
    
    private final boolean isToString;
    private final boolean isGetStackTrace;

    private int access;
    
    public HologramMethodGenerator(String owner, int access, String name, String desc, MethodVisitor superVisitor, boolean isToString, boolean isGetStackTrace) {
        super(Opcodes.ASM4, null);
        this.analyzer = new AnalyzerAdapter(owner, access, name, desc, superVisitor);
        this.mv = analyzer;
        this.name = name;
        this.owner = Type.getObjectType(owner);
        this.access = access;
        this.methodType = Type.getMethodType(desc);
        this.isToString = isToString;
        this.isGetStackTrace = isGetStackTrace;
        
        activeMethod = name + desc;
    }

    public void getClassMirror(Type type) {
	if (type.getSort() == Type.OBJECT && type.getInternalName().startsWith("hologram")) {
	    getstatic(type.getInternalName(), "classMirror", classMirrorType.getDescriptor());
	} else {
	    getstatic(owner.getInternalName(), "classMirror", classMirrorType.getDescriptor());
	    aconst(type.getDescriptor());
	    new MethodHandle() {
        	protected void methodCall() throws Throwable {
        	    ObjectHologram.getClassMirrorForType(null, null);
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
        // Various special cases here to adjust for the fact that Object and Throwable
        // are still in the hierarchy and have some methods with incompatible types.
        
        Type stringHologramType = getHologramType(String.class);
        if (name.equals("toString") && desc.equals(Type.getMethodDescriptor(stringHologramType))) {
            desc = Type.getMethodDescriptor(Type.getType(String.class));
            if (owner.equals(Type.getInternalName(Hologram.class))) {
                owner = OBJECT_TYPE.getInternalName();
                // Handle calling Object.toString() with an invokespecial opcode, 
                // which doesn't work any more since we've changed the superclass.
                if (opcode == Opcodes.INVOKESPECIAL) {
                    opcode = Opcodes.INVOKESTATIC;
                    owner = objectHologramType.getInternalName();
                    name = "hologramToString";
                    desc = Type.getMethodDescriptor(stringType, hologramType);
                }
            }
            
            super.visitMethodInsn(opcode, owner, name, desc);
            
            getClassMirror(this.owner);
            invokestatic(objectHologramType.getInternalName(),
                         "makeStringHologram",
                         Type.getMethodDescriptor(hologramType, stringType, classMirrorType));
            checkcast(stringHologramType);
            return;
        }
        
        if (name.equals("getClass") && desc.equals(Type.getMethodDescriptor(getHologramType(Class.class)))) {
            invokeinterface(Type.getInternalName(Hologram.class), 
                    "getMirror", 
                    Type.getMethodDescriptor(objectMirrorType));
            invokeinterface(objectMirrorType.getInternalName(),
                            "getClassMirror",
                            Type.getMethodDescriptor(Type.getType(ClassMirror.class)));
            
            invokestatic(objectHologramType.getInternalName(),
                         "make",
                         Type.getMethodDescriptor(hologramType, objectMirrorType));
            checkcast(getHologramType(Class.class));
            return;
        }
        
        if (owner.equals(Type.getInternalName(Hologram.class))) {
            if (name.equals("<init>") && this.owner.equals(getHologramType(Throwable.class))) {
                owner = Type.getInternalName(Throwable.class);
            } else if (name.equals("<init>") || name.equals("toString")) {
                owner = objectHologramType.getInternalName();
            } else {
                owner = OBJECT_TYPE.getInternalName();
            }
        }
        
        if (name.equals("clone")) {
            if (desc.equals(Type.getMethodDescriptor(hologramType))) {
                desc = Type.getMethodDescriptor(OBJECT_TYPE);
                
                // Object.clone() on an array type is legal since array types
                // are treated specially by the JVM, but the mirror interfaces 
                // are not granted the same leeway.
                Object t = stackType(0);
                if (t instanceof String) {
                    String internalName = (String)t;
                    Type type = Type.getObjectType(internalName);
                    if (HologramClassGenerator.getOriginalType(type).getSort() == Type.ARRAY) {
                        owner = internalName;
                    }
                }
            } 
            if (owner.equals(Type.getType(ObjectArrayMirror.class).getInternalName()) ||
                            (owner.startsWith("hologramarray") && !owner.startsWith("hologramarrayimpl"))) {
                String originalName = getOriginalInternalClassName(owner);
                owner = getHologramType(Type.getObjectType(originalName), true).getInternalName();
                checkcast(Type.getObjectType(owner));
            }
        }
        
//        if (owner.equals(getHologramType(Throwable.class).getInternalName())) {
//            if (name.equals("<init>") && desc.equals(Type.getMethodDescriptor(Type.VOID_TYPE, getHologramType(String.class)))) {
//                desc = Type.getMethodDescriptor(Type.VOID_TYPE, objectHologramType);
//            }
//        }
        
        if (owner.equals(hologramThrowableType.getInternalName()) && name.equals("fillInStackTrace") && desc.equals(Type.getMethodDescriptor(hologramThrowableType))) {
            desc = Type.getMethodDescriptor(throwableType);
            owner = throwableType.getInternalName();
        }
        
        if (name.equals("equals") && desc.equals(Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(Hologram.class)))) {
            desc = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, OBJECT_TYPE);
        }
        
        if (name.equals("<init>") && !owner.equals(Type.getInternalName(Throwable.class))) {
            int argsSize = Type.getArgumentsAndReturnSizes(desc) >> 2;
            desc = HologramClassGenerator.addMirrorParam(desc);
            
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
            invokestatic(Type.getInternalName(ObjectHologram.class),
                         "cleanStackTraceElement",
                         Type.getMethodDescriptor(steType, steType));
        }
    }
    
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        boolean isSet = (opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC);
        boolean isStatic = (opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC);

        Type fieldType = Type.getType(desc);
        int setValueLocal = -1;
        
        if (isStatic) {
            // For a static field the instance is null
            if (isSet) {
                setValueLocal = lvs.newLocal(fieldType);
                store(setValueLocal, fieldType);
            }
            aconst(null);
            if (isSet) {
                load(setValueLocal, fieldType);
            }
        } else {
            // If this is an "uninitialized this", the mirror is the nth argument instead
            // of the mirror field on ObjectHologram.
            Object stackType = stackType(isSet ? 1 : 0);
            if (stackType == Opcodes.UNINITIALIZED_THIS) {
                // Pop the original argument
                if (isSet) {
                    setValueLocal = lvs.newLocal(fieldType);
                    store(setValueLocal, fieldType);
                }

                pop();
                load((methodType.getArgumentsAndReturnSizes() >> 2) - 1, instanceMirrorType);

                MethodHandle.OBJECT_HOLOGRAM_MAKE.invoke(this);

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
            fieldTypeForMirrorCall = hologramType;
        } else {
            suffix = HologramClassGenerator.getSortName(fieldSort);
        }
        
        // Call the appropriate getter/setter method on the mirror
        String methodDesc;
        if (isSet) {
            methodDesc = Type.getMethodDescriptor(Type.VOID_TYPE, hologramType, fieldTypeForMirrorCall, classMirrorType, stringType);
        } else {
            methodDesc = Type.getMethodDescriptor(fieldTypeForMirrorCall, hologramType, classMirrorType, stringType);
        }
        invokestatic(instanceHologramType.getInternalName(), 
                     (isSet ? "set" : "get") + suffix + "Field", 
                     methodDesc);
        
        if (!isSet && fieldTypeForMirrorCall.equals(hologramType)) {
            checkcast(fieldType);
        }
    }
    
    private Object stackType(int indexFromTop) {
        return analyzer.stack.get(analyzer.stack.size() - 1 - indexFromTop);
    }
    
    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
        Type hologramArrayType = Type.getType(desc);
        Type originalElementType = Type.getObjectType(getOriginalInternalClassName(hologramArrayType.getInternalName())).getElementType();
        
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
        
        anew(hologramArrayType);
        dup();
        
        getClassMirror(originalElementType);
        load(dimsArrayVar, intArrayType);
        invokeinterface(classMirrorType.getInternalName(),
                "newArray",
                Type.getMethodDescriptor(Type.getType(ArrayMirror.class), intArrayType));
        
        invokespecial(hologramArrayType.getInternalName(), 
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
            case Opcodes.AALOAD: arrayElementType = hologramType; break;
            case Opcodes.BALOAD: arrayElementType = Type.BYTE_TYPE; break;
            case Opcodes.CALOAD: arrayElementType = Type.CHAR_TYPE; break;
            case Opcodes.SALOAD: arrayElementType = Type.SHORT_TYPE; break;
            case Opcodes.IASTORE: arrayElementType = Type.INT_TYPE; break;
            case Opcodes.LASTORE: arrayElementType = Type.LONG_TYPE; break;
            case Opcodes.FASTORE: arrayElementType = Type.FLOAT_TYPE; break;
            case Opcodes.DASTORE: arrayElementType = Type.DOUBLE_TYPE; break;
            case Opcodes.AASTORE: arrayElementType = hologramType; break;
            case Opcodes.BASTORE: arrayElementType = Type.BYTE_TYPE; break;
            case Opcodes.CASTORE: arrayElementType = Type.CHAR_TYPE; break;
            case Opcodes.SASTORE: arrayElementType = Type.SHORT_TYPE; break;
            }
            
            Type mirrorType = HologramClassGenerator.objectArrayMirrorType;
            if (arrayElementType.getSort() != Type.OBJECT && arrayElementType.getSort() != Type.ARRAY) {
                mirrorType = getPrimitiveArrayMirrorType(arrayElementType);
            }
            
            // Use the analyzer to figure out the expected array element type
            Type arrayElementTypeForMirrorCall = arrayElementType;
            Type hologramArrayType = Type.getObjectType((String)stackType(isArrayStore ? 1 + arrayElementType.getSize() : 1));
            if (hologramArrayType == null) {
                hologramArrayType = Type.getType(ObjectArrayHologram.class);
            }
            if (arrayElementType.equals(hologramType)) {
                Type originalType = Type.getObjectType(getOriginalInternalClassName(hologramArrayType.getInternalName()));
                arrayElementType = getHologramType(Reflection.makeArrayType(originalType.getDimensions() - 1, originalType.getElementType()));
                hologramArrayType = Type.getType(ObjectArrayHologram.class);
            }
            
            // Call the appropriate getter/setter method on the hologram
            String methodDesc;
            if (isArrayStore) {
                methodDesc = Type.getMethodDescriptor(Type.VOID_TYPE, mirrorType, Type.INT_TYPE, arrayElementTypeForMirrorCall);
            } else {
                methodDesc = Type.getMethodDescriptor(arrayElementTypeForMirrorCall, mirrorType, Type.INT_TYPE);
            }
            invokestatic(hologramArrayType.getInternalName(), 
                            (isArrayStore ? "setHologram" : "getHologram"), 
                            methodDesc);
            if (!isArrayStore && arrayElementTypeForMirrorCall.equals(hologramType)) {
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
                invokestatic(objectHologramType.getInternalName(),
                             "getRealStringForHologram",
                             Type.getMethodDescriptor(Type.getType(String.class), Type.getType(ObjectHologram.class)));
            } else if (isGetStackTrace) {
                invokestatic(objectHologramType.getInternalName(),
                             "getRealStackTraceForHologram",
                             Type.getMethodDescriptor(Type.getType(StackTraceElement[].class), Type.getType(Hologram.class)));
            }
        }
        
        if (opcode == Opcodes.RETURN && owner.equals(hologramThrowableType) && name.equals("<init>")) {
            load(0, owner);
            new MethodHandle() {
                protected void methodCall() throws Throwable {
                    ObjectHologram.register(null);;
                }
            }.invoke(this);
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
            
            // Instantiate the hologram class
            Type hologramArrayType = getHologramType(arrayType, true);
            anew(hologramArrayType);
            dupX1();
            swap();
            
            invokespecial(hologramArrayType.getInternalName(), 
                          "<init>", 
                          Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(ObjectArrayMirror.class)));
            return;
        }
        
        if (opcode == Opcodes.NEW && type.equals(Type.getInternalName(Hologram.class))) {
            type = objectHologramType.getInternalName();
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
            
            String arrayHologramType = "edu/ubc/mirrors/holograms/" + getSortName(elementType.getSort()) + "ArrayHologram";
            
            invokestatic(objectHologramType.getInternalName(),
                    "make",
                    Type.getMethodDescriptor(hologramType, objectMirrorType));
            checkcast(Type.getObjectType(arrayHologramType));
            
            return;
        }
        
        super.visitIntInsn(opcode, operand);
    }
    
    @Override
    public void visitLdcInsn(Object cst) {
        super.visitLdcInsn(cst);
        
        if (cst instanceof String) {
            Type hologramStringType = getHologramType(stringType);
            getClassMirror(this.owner);
            invokestatic(objectHologramType.getInternalName(),
                         "makeStringHologram",
                         Type.getMethodDescriptor(hologramType, stringType, classMirrorType));
            checkcast(hologramStringType);
        } else if (cst instanceof Type) {
            Type hologramClassType = getHologramType(classType);
            getClassMirror(this.owner);
            invokestatic(objectHologramType.getInternalName(),
                         "makeClassHologram",
                         Type.getMethodDescriptor(hologramType, classType, classMirrorType));
            checkcast(hologramClassType);
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
    	    ObjectHologram.getClassMirrorForHolographicClass(null);
    	}
        }.invoke(mv);
        mv.dup();
        mv.putstatic(owner.getInternalName(), "classMirror", classMirrorType.getDescriptor());
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

            if (owner.equals(getHologramType(Throwable.class))) {
                load(0, owner);
                load((methodType.getArgumentsAndReturnSizes() >> 2) - 1, instanceMirrorType);
                putfield(owner.getInternalName(), "mirror", Type.getDescriptor(ObjectMirror.class));
            }
        }
    }
    
    private void boxIfNeeded(Type type) {
        Class<?> boxingType = Reflection.getBoxingType(type);
        if (boxingType != null) {
            invokestatic(Type.getInternalName(boxingType), "valueOf", Type.getMethodType(Type.getType(boxingType), type).getDescriptor());
        }
    }
    
    public void generateNativeThunk() {
        visitCode();
        
        getClassMirror(owner);
        aconst(name);
        aconst(HologramClassGenerator.getOriginalType(methodType).getDescriptor());
        
        Type[] parameterTypes = methodType.getArgumentTypes();
        int var = 0;
        if ((Opcodes.ACC_STATIC & access) == 0) {
            load(var, owner);
            MethodHandle.OBJECT_HOLOGRAM_GET_MIRROR.invoke(this);
            var++;
        } else {
            aconst(null);
        }
        aconst(parameterTypes.length);
        newarray(OBJECT_TYPE);
        
        for (int param = 0; param < parameterTypes.length; param++) {
            Type paramType = parameterTypes[param];
            dup();
            aconst(param);
            load(var, paramType);
            boxIfNeeded(paramType);
            if (HologramClassGenerator.isRefType(paramType)) {
                MethodHandle.OBJECT_HOLOGRAM_GET_MIRROR.invoke(this);
            }
            astore(OBJECT_TYPE);
            var += paramType.getSize();
        }
        
        MethodHandle.OBJECT_HOLOGRAM_INVOKE_METHOD_HANDLER.invoke(this);
        
        Type returnType = methodType.getReturnType();
        if (returnType.getSort() != Type.VOID) {
            Class<?> boxingType = Reflection.getBoxingType(returnType);
            if (boxingType != null) {
                checkcast(Type.getType(boxingType));
                invokevirtual(Type.getInternalName(boxingType), returnType.getClassName() + "Value", Type.getMethodType(returnType).getDescriptor());
            } else {
                getClassMirror(owner);
                MethodHandle.OBJECT_HOLOGRAM_MAKE_FROM_OBJECT.invoke(this);
                checkcast(returnType);
            }
        }
        areturn(returnType);
        visitMaxs(Math.max(2, var + 1), Math.max(2, var + 1));
        visitEnd();
    }
}

