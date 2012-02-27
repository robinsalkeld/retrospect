package edu.ubc.mirrors.mirages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.SimpleVerifier;

/**
 * @author robinsalkeld
 */
public class FrameVerifier extends Interpreter<FrameValue> implements Opcodes {
    
    private final SimplerVerifier simplerVerifier;
    
    private static class SimplerVerifier extends SimpleVerifier {
        
        /**
         * The super class of the class that is verified.
         */
        private final Type currentSuperClass;

        /**
         * The interfaces implemented by the class that is verified.
         */
        private final List<Type> currentClassInterfaces;

        /**
         * If the class that is verified is an interface.
         */
        private final boolean isInterface;
        
        // TODO-RS: This should be per-classloader
        private static final Map<Type, SimplerVerifier> activeVerifiers = new HashMap<Type, SimplerVerifier>();
        
        public SimplerVerifier(
                final Type currentClass,
                final Type currentSuperClass,
                final List<Type> currentClassInterfaces,
                final boolean isInterface) {
            this.currentSuperClass = currentSuperClass;
            this.currentClassInterfaces = currentClassInterfaces;
            this.isInterface = isInterface;
            
            activeVerifiers.put(currentClass, this);
        }
        
        public void discard() {
            activeVerifiers.remove(this);
        }
        
        @Override
        protected boolean isInterface(Type t) {
            SimplerVerifier activeVerifier = activeVerifiers.get(t);
            if (activeVerifier != null) {
                return activeVerifier.isInterface;
            }
            
            return getClass(t).isInterface();
        }
        
        public Type getSuperClass(Type t) {
            SimplerVerifier activeVerifier = activeVerifiers.get(t);
            if (activeVerifier != null) {
                return activeVerifier.currentSuperClass;
            }
            
            Class<?> c = getClass(t).getSuperclass();
            return c == null ? null : Type.getType(c);
        }
        
        protected boolean isAssignableFrom(Type t, Type u) {
            // Copied from superclass - not convinced it's 100% right
            if (t.equals(u)) {
                return true;
            }
            SimplerVerifier tVerifier = activeVerifiers.get(t);
            if (tVerifier != null) {
                if (getSuperClass(u) == null) {
                    return false;
                } else {
                    if (tVerifier.isInterface) {
                        return u.getSort() == Type.OBJECT || u.getSort() == Type.ARRAY;
                    }
                    return isAssignableFrom(t, getSuperClass(u));
                }
            }
            SimplerVerifier uVerifier = activeVerifiers.get(u);
            if (uVerifier != null) {
                if (isAssignableFrom(t, uVerifier.currentSuperClass)) {
                    return true;
                }
                if (uVerifier.currentClassInterfaces != null) {
                    for (int i = 0; i < uVerifier.currentClassInterfaces.size(); ++i) {
                        Type v = uVerifier.currentClassInterfaces.get(i);
                        if (isAssignableFrom(t, v)) {
                            return true;
                        }
                    }
                }
                return false;
            }
            Class<?> tc = getClass(t);
            if (tc.isInterface()) {
                tc = Object.class;
            }
            return tc.isAssignableFrom(getClass(u));
        }
    }
    
    private FrameAnalyzer analyzer;
    
        
    
    private Frame<FrameValue> currentFrame;
    private int currentInsnIndex;
    
    public FrameVerifier(
            final Type currentClass,
            final Type currentSuperClass,
            final List<Type> currentClassInterfaces,
            final boolean isInterface) {
        super(ASM4);
        simplerVerifier = new SimplerVerifier(currentClass, currentSuperClass, currentClassInterfaces, isInterface);
    }
        
    public void discard() {
        simplerVerifier.discard();
    }
    
    public void setAnalyzer(FrameAnalyzer analyzer) {
        this.analyzer = analyzer;
    }
    
    @Override
    public FrameValue newOperation(AbstractInsnNode insn) throws AnalyzerException {
        BasicValue result = simplerVerifier.newOperation(insn);
        if (insn.getOpcode() == Opcodes.NEW) {
            return new FrameValue(result, currentInsnIndex);
        } else {
            return FrameValue.fromBasicValue(result);
        }
    }
    
    @Override
    public FrameValue naryOperation(AbstractInsnNode insn, List<? extends FrameValue> values) throws AnalyzerException {
        if (insn.getOpcode() == Opcodes.INVOKESPECIAL) {
            MethodInsnNode methodNode = (MethodInsnNode)insn;
            if (methodNode.name.charAt(0) == '<') {
                FrameValue target = values.get(0);
                Type targetType = target.getBasicValue().getType();
                Type ownerType = Type.getObjectType(methodNode.owner);
                BasicValue owner = new BasicValue(ownerType);
                FrameValue expected = new FrameValue(owner, 0);
                if (!target.isUninitialized()) {
                    throw new AnalyzerException(insn,
                            "Argument 0",
                            expected,
                            target);
                }
                if (!targetType.equals(ownerType) && !(target.isUninitializedThis() && simplerVerifier.getSuperClass(targetType).equals(ownerType))) {
                    throw new AnalyzerException(insn,
                            "Argument 0",
                            expected,
                            target);
                }
                FrameValue result = FrameValue.fromBasicValue(target.getBasicValue());
                int locals = currentFrame.getLocals();
                for (int local = 0; local < locals; local++) {
                    if (currentFrame.getLocal(local) == target) {
                        currentFrame.setLocal(local, result);
                    }
                }
                Stack<FrameValue> stack = new Stack<FrameValue>();
                while (currentFrame.getStackSize() > 0) {
                    FrameValue value = currentFrame.pop();
                    if (value == target) {
                        value = result;
                    }
                    stack.push(value);
                }
                while (!stack.empty()) {
                    currentFrame.push(stack.pop());
                }
            }
        }
        
        List<BasicValue> basicValues = new ArrayList<BasicValue>(values.size());
        for (FrameValue value : values) {
            basicValues.add(value.getBasicValue());
        }
        
        return FrameValue.fromBasicValue(simplerVerifier.naryOperation(insn, basicValues));
    }
    
    @Override
    public FrameValue copyOperation(AbstractInsnNode insn, FrameValue value) throws AnalyzerException {
        if (value.isUninitialized()) {
            int opcode = insn.getOpcode();
            if (opcode == ALOAD || opcode == ASTORE || (DUP <= opcode && opcode <= SWAP)) {
                return value;
            } else {
                throw new AnalyzerException(insn, "value");
            }
        }

        return FrameValue.fromBasicValue(simplerVerifier.copyOperation(insn, value.getBasicValue()));
    }
    
    @Override
    public FrameValue binaryOperation(AbstractInsnNode insn, FrameValue value1, FrameValue value2) throws AnalyzerException {
        BasicValue basicValue1;
        if (insn.getOpcode() == PUTFIELD) {
            basicValue1 = value1.getBasicValue();
        } else {
            basicValue1 = checkUninitialized(insn, "value 1", value1);
        }
        
        return FrameValue.fromBasicValue(simplerVerifier.binaryOperation(insn, 
                basicValue1, 
                checkUninitialized(insn, "value 2", value2)));
    }

    @Override
    public FrameValue newValue(Type type) {
        return FrameValue.fromBasicValue(simplerVerifier.newValue(type));
    }
    
    @Override
    public FrameValue merge(FrameValue v, FrameValue w) {
        if (v.equals(w)) {
            return v;
        } else if (v.isUninitialized() || w.isUninitialized()) {
            return FrameValue.fromBasicValue(BasicValue.UNINITIALIZED_VALUE);
        }
        return FrameValue.fromBasicValue(simplerVerifier.merge(v.getBasicValue(), w.getBasicValue()));
    }
    
    @Override
    public FrameValue ternaryOperation(AbstractInsnNode insn,
            FrameValue value1, FrameValue value2, FrameValue value3)
            throws AnalyzerException {
        return FrameValue.fromBasicValue(simplerVerifier.ternaryOperation(insn, 
                checkUninitialized(insn, "value1", value1), 
                checkUninitialized(insn, "value2", value2), 
                checkUninitialized(insn, "value3", value3)));
    }
    
    @Override
    public void returnOperation(AbstractInsnNode insn, FrameValue value, FrameValue expected) throws AnalyzerException {
        simplerVerifier.returnOperation(insn, 
                checkUninitialized(insn, "value", value), 
                expected.getBasicValue());
    }
    
    @Override
    public FrameValue unaryOperation(AbstractInsnNode insn, FrameValue value) throws AnalyzerException {
        return FrameValue.fromBasicValue(simplerVerifier.unaryOperation(insn, 
                checkUninitialized(insn, "value", value)));
    }
    
    private BasicValue checkUninitialized(AbstractInsnNode insn, String msg, FrameValue value) throws AnalyzerException {
        if (value.isUninitialized()) {
            throw new AnalyzerException(insn, msg);
        }
        return value.getBasicValue();
    }

    public void setClassLoader(ClassLoader loader) {
        simplerVerifier.setClassLoader(loader);
    }

    public void setContext(int index, Frame<FrameValue> frame) {
        this.currentInsnIndex = index;
        System.out.println(index);
        this.currentFrame = frame;
    }
}
