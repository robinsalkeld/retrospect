package edu.ubc.mirrors.mirages;

import java.util.ArrayList;
import java.util.List;
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

/**
 * @author robinsalkeld
 */
public class FrameVerifier extends Interpreter<FrameValue> implements Opcodes {
    
    private final BetterVerifier simplerVerifier;
    
    private Frame<FrameValue> currentFrame;
    
    public FrameVerifier(ClassHierarchy hierarchy) {
        super(ASM4);
        simplerVerifier = new BetterVerifier(hierarchy);
    }
    
    @Override
    public FrameValue newOperation(AbstractInsnNode insn) throws AnalyzerException {
        BasicValue result = simplerVerifier.newOperation(insn);
        if (insn.getOpcode() == Opcodes.NEW) {
            return new FrameValue(result, insn);
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
                FrameValue expected = new FrameValue(owner, insn);
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
        try {
            simplerVerifier.returnOperation(insn, 
                    checkUninitialized(insn, "value", value), 
                    expected.getBasicValue());
        } catch (AnalyzerException e) {
            simplerVerifier.returnOperation(insn, 
                    checkUninitialized(insn, "value", value), 
                    expected.getBasicValue());
        }
    }
    
    @Override
    public FrameValue unaryOperation(AbstractInsnNode insn, FrameValue value) throws AnalyzerException {
        return FrameValue.fromBasicValue(simplerVerifier.unaryOperation(insn, 
                checkUninitialized(insn, "value", value)));
    }
    
    private BasicValue checkUninitialized(AbstractInsnNode insn, String msg, FrameValue value) throws AnalyzerException {
        if (value.isUninitialized()) {
            throw new AnalyzerException(insn, msg, "initialized value", value);
        }
        return value.getBasicValue();
    }

    public void setContext(Frame<FrameValue> frame) {
        this.currentFrame = frame;
    }
}
