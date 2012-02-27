package edu.ubc.mirrors.mirages;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;

public class FrameAnalyzer extends Analyzer<FrameValue> {

    FrameVerifier verifier;
    
    public FrameAnalyzer(FrameVerifier verifier) {
        super(verifier);
        this.verifier = verifier;
        verifier.setAnalyzer(this);
    }

    private final Map<AbstractInsnNode, Integer> insnIndices = new HashMap<AbstractInsnNode, Integer>();
    
    @Override
    protected void init(String owner, MethodNode m) throws AnalyzerException {
        super.init(owner, m);
        
        for (int i = 0; i < m.instructions.size(); i++) {
            insnIndices.put(m.instructions.get(i), i);
        }
        
        if (m.name.equals("<init>")) {
            Frame<FrameValue>[] frames = getFrames();
            FrameValue initializedValue = frames[0].getLocal(0);
            FrameValue uninitializedThis = FrameValue.makeUninitializedThis(initializedValue.getBasicValue());
            frames[0].setLocal(0, uninitializedThis); 
        }
    }
    
    public int getInsnIndex(AbstractInsnNode insn) {
        return insnIndices.get(insn);
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
            int index = getInsnIndex(insn);
            ((FrameVerifier)interpreter).setContext(index, this);
            super.execute(insn, interpreter);
        }
    }
    
    protected Frame<FrameValue> newFrame(final int nLocals, final int nStack) {
        return new ContextFrame(nLocals, nStack);
    }
    
    protected Frame<FrameValue> newFrame(final Frame<? extends FrameValue> src) {
        return new ContextFrame(src);
    }
    
}
