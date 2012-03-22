package edu.ubc.mirrors.mirages;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;

public class FrameAnalyzer extends Analyzer<FrameValue> {

    public FrameAnalyzer(FrameVerifier verifier) {
        super(verifier);
    }

    private final Map<AbstractInsnNode, Integer> insnIndices = new HashMap<AbstractInsnNode, Integer>();
    
    private boolean[] jumpIn;
    private boolean[] stepIn;
    private AbstractInsnNode currentInsn;
    private MethodNode m;
    
    @Override
    protected void init(String owner, MethodNode m) throws AnalyzerException {
        super.init(owner, m);
        
        this.m = m;
        int n = m.instructions.size();
        this.jumpIn = new boolean[n];
        this.stepIn = new boolean[n];
        
        this.stepIn[0] = true;
        
        for (int i = 0; i < n; i++) {
            insnIndices.put(m.instructions.get(i), i);
        }
        
        if (m.name.equals("<init>")) {
            Frame<FrameValue>[] frames = getFrames();
            FrameValue initializedValue = frames[0].getLocal(0);
            FrameValue uninitializedThis = FrameValue.makeUninitializedThis(initializedValue.getBasicValue());
            frames[0].setLocal(0, uninitializedThis); 
        }
    }
    
    @Override
    protected void newControlFlowEdge(int insn, int successor) {
        if (successor == insn + 1 && 
                (currentInsn == null || 
                (currentInsn.getOpcode() != Opcodes.TABLESWITCH &&
                 currentInsn.getOpcode() != Opcodes.LOOKUPSWITCH))) {
            stepIn[successor] = true;
        } else {
            jumpIn[successor] = true;
        }
        
        super.newControlFlowEdge(insn, successor);
    }
    
    @Override
    protected boolean newControlFlowExceptionEdge(int insn, int successor) {
        jumpIn[successor] = true;
        
        return super.newControlFlowExceptionEdge(insn, successor);
    }
    
    private class ContextFrame extends Frame<FrameValue> {

        public ContextFrame(Frame<? extends FrameValue> src) {
            super(src);
        }

        public ContextFrame(final int nLocals, final int nStack) {
            super(nLocals, nStack);
        }
        
        @Override
        public void execute(AbstractInsnNode insn, Interpreter<FrameValue> interpreter) throws AnalyzerException {
            ((FrameVerifier)interpreter).setContext(this);
           FrameAnalyzer.this.currentInsn = insn;
            super.execute(insn, interpreter);
        }
    }
    
    protected Frame<FrameValue> newFrame(final int nLocals, final int nStack) {
        return new ContextFrame(nLocals, nStack);
    }
    
    protected Frame<FrameValue> newFrame(final Frame<? extends FrameValue> src) {
        return new ContextFrame(src);
    }
    
    public Frame<FrameValue>[] insertFrames() {
        Frame<FrameValue>[] frames = getFrames();
        if (m == null || m.instructions == null) {
            return frames;
        }
        
        List<Frame<FrameValue>> newFrames = new ArrayList<Frame<FrameValue>>();
        AbstractInsnNode insnNode = m.instructions.getFirst();
        int n = m.instructions.size();
        for (int i = 0; i < n; i++) {
            int insnType = insnNode.getType();
            AbstractInsnNode nextNode = insnNode.getNext();
            
            if (insnType == AbstractInsnNode.LABEL
                    || insnType == AbstractInsnNode.LINE
                    || insnType == AbstractInsnNode.FRAME) {
                if (i + 1 < n) {
                    jumpIn[i + 1] |= jumpIn[i];
                    stepIn[i + 1] = stepIn[i];
                }
                
                if (insnType == AbstractInsnNode.FRAME) {
                    m.instructions.remove(insnNode);
                } else {
                    newFrames.add(frames[i]);
                }
            } else {
                if (jumpIn[i] || !stepIn[i]) {
                    Frame<FrameValue> frame = frames[i];
                    FrameNode frameNode = toFrameNode(frame);
                    m.instructions.insertBefore(insnNode, frameNode);
                    newFrames.add(newFrame(frame));
                }
                newFrames.add(frames[i]);
            }
            
            insnNode = nextNode;
        }
        
        @SuppressWarnings("unchecked")
        Frame<FrameValue>[] result = (Frame<FrameValue>[])newFrames.toArray(new Frame<?>[newFrames.size()]); 
        return result;
    }
    
    private FrameNode toFrameNode(Frame<FrameValue> frame) {
        int numLocals = frame.getLocals();
        List<Object> localsList = new ArrayList<Object>(frame.getLocals());
        for (int i = 0; i < numLocals; i++) {
            FrameValue value = frame.getLocal(i);
            localsList.add(toFrameObject(value));
            if (value.getSize() == 2) {
                // Skip the next TOP - the visitor API represents locals
                // with one element instead.
                i++;
            }
        }
        Object[] locals = localsList.toArray();
        
        int stackSize = frame.getStackSize();
        Object[] stack = new Object[stackSize];
        for (int i = 0; i < stackSize; i++) {
            FrameValue value = frame.getStack(i);
            stack[i] = toFrameObject(value);
        }
        
        return new FrameNode(Opcodes.F_NEW, locals.length, locals, stack.length, stack);
    }
        
    public Object toFrameObject(FrameValue value) {
        if (value.isUninitializedThis()) {
            return Opcodes.UNINITIALIZED_THIS;
        } else if (value.isUninitialized()) {
            AbstractInsnNode newInsn = value.getNewInsn(); 
            // TODO: Could also search up to the previous actual instruction...
            AbstractInsnNode labelNode = newInsn.getPrevious();
            if (labelNode == null || !(labelNode instanceof LabelNode)) {
                labelNode = new LabelNode();
                m.instructions.insertBefore(newInsn, labelNode);
            }
            return ((LabelNode)labelNode).getLabel();
        } else {
            Type type = value.getBasicValue().getType();
            if (type == null) {
                return Opcodes.TOP;
            } else {
                switch (type.getSort()) {
                case Type.VOID:
                    // TODO: Not sure what to do here. Used for BasicValue.RETURNADDRESS_VALUE,
                    // but I can't figure out the correct verifier type for the return addresses
                    // used by JSR/RET.
                    return Opcodes.TOP;
                case Type.BOOLEAN:
                case Type.BYTE:
                case Type.CHAR:
                case Type.SHORT:
                case Type.INT:
                    return Opcodes.INTEGER;
                case Type.LONG:
                    return Opcodes.LONG;
                case Type.FLOAT:
                    return Opcodes.FLOAT;
                case Type.DOUBLE:
                    return Opcodes.DOUBLE;
                case Type.OBJECT:
                case Type.ARRAY:
                    return type.getInternalName();
                case Type.METHOD:
                    // Shouldn't happen
                default:
                    throw new RuntimeException("Bad sort: " + type.getSort());
                }
            }
        }
    }
    
    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        FrameAnalyzerAdaptor.printAnalyzerResult(m, getFrames(), pw);
        return sw.toString();
    }
}
