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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;

import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ClassLoaderHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;

/**
 * @author robinsalkeld
 */
public class FrameVerifier extends Interpreter<FrameValue> implements Opcodes {
    
    private final BetterVerifier simplerVerifier;
    
    private Frame<FrameValue> currentFrame;
    
    public FrameVerifier(VirtualMachineMirror vm, ClassMirrorLoader loader, boolean holograms) {
        super(ASM4);
        simplerVerifier = holograms ? 
                new HologramVerifier((VirtualMachineHolograph)vm, (ClassLoaderHolograph)loader) : 
                new BetterVerifier(vm, loader);
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
