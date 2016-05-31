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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.VirtualMachineMirror;

public class FrameAnalyzerAdaptor extends ClassVisitor {

    private final VirtualMachineMirror vm;
    private final ClassMirrorLoader loader;
    private final boolean insertFrames;
    private final boolean holograms;
    private Type thisType = null;
    
    public FrameAnalyzerAdaptor(VirtualMachineMirror vm, ClassMirrorLoader loader, ClassVisitor cv, boolean insertFrames, boolean holograms) {
        super(Opcodes.ASM5, cv);
        this.vm = vm;
        this.loader = loader;
        this.insertFrames = insertFrames;
        this.holograms = holograms;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.thisType = Type.getObjectType(name);
        super.visit(version, access, name, signature, superName, interfaces);
    }
    
    static void printAnalyzerResult(
            MethodNode method,
            Frame<FrameValue>[] frames,
            final PrintWriter pw)
    {
        Textifier t = new Textifier();
        TraceMethodVisitor mv = new TraceMethodVisitor(t);

        pw.println(method.name + method.desc);
        for (int j = 0; j < method.instructions.size(); ++j) {
            method.instructions.get(j).accept(mv);

            StringBuffer s = new StringBuffer();
            Frame<FrameValue> f = frames[j];
            if (f == null) {
                s.append('?');
            } else {
                for (int k = 0; k < f.getLocals(); ++k) {
                    s.append(getShortName(f.getLocal(k).toString()))
                            .append(' ');
                }
                s.append(" : ");
                for (int k = 0; k < f.getStackSize(); ++k) {
                    s.append(getShortName(f.getStack(k).toString()))
                            .append(' ');
                }
            }
            while (s.length() < method.maxStack + method.maxLocals + 1) {
                s.append(' ');
            }
            pw.print(Integer.toString(j + 100000).substring(1));
            pw.print(" " + s + " : " + t.text.get(t.text.size() - 1));
        }
        for (int j = 0; j < method.tryCatchBlocks.size(); ++j) {
            method.tryCatchBlocks.get(j).accept(mv);
            pw.print(" " + t.text.get(t.text.size() - 1));
        }
        pw.println();
    }

    private static String getShortName(final String name) {
        int n = name.lastIndexOf('/');
        int k = name.length();
        if (name.charAt(k - 1) == ';') {
            k--;
        }
        return (name.startsWith("(uninitialized)") ? "(U)" : "") + (n == -1 ? name : name.substring(n + 1, k));
    }
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        final MethodVisitor superVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        final Map<Label, LabelNode> labelNodes = new HashMap<Label, LabelNode>();
        final FrameVerifier verifier = new FrameVerifier(vm, loader, holograms);
        
        MethodNode analyzer = new MethodNode(Opcodes.ASM5, access, name, desc, null, null) {
            @Override
            public void visitEnd() {
                FrameAnalyzer a = new FrameAnalyzer(verifier);
                Frame<FrameValue>[] frames = null;
                try {
                    frames = a.analyze(thisType.getInternalName(), this);
                    if (superVisitor != null) {
                        if (insertFrames) {
                            frames = a.insertFrames();
                        }
                        accept(superVisitor);
                    }
                } catch (Throwable e) {
                    if (e instanceof IndexOutOfBoundsException
                            && maxLocals == 0 && maxStack == 0)
                    {
                        throw new RuntimeException("Data flow checking option requires valid, non zero maxLocals and maxStack values.");
                    }
                    if (frames == null) {
                        frames = a.getFrames();
                    }
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw, true);
                    printAnalyzerResult(this, frames, pw);
                    pw.close();
                    throw new RuntimeException(sw.toString(), e);
                }
            }
            
            @Override
            protected LabelNode getLabelNode(Label l) {
                LabelNode node = labelNodes.get(l);
                if (node == null) {
                    node = new LabelNode(l);
                    labelNodes.put(l, node);
                }
                return node;
            }
        };
        
        analyzer.instructions = new FrameInsnList();
        
        // Inline subroutines since other pieces of the pipeline can't handle them
        return new JSRInlinerAdapter(analyzer, access, name, desc, signature, exceptions);
    }
    
    private class FrameInsnList extends InsnList {
        
        @Override
        public void accept(MethodVisitor mv) {
            final int size = size();
            for (int i = 0; i < size; i++) {
                try {
                    get(i).accept(mv);
                } catch (RuntimeException e) {
                    throw new RuntimeException("Error at instruction " + i, e);
                }
            }
        }
        
    }
}
