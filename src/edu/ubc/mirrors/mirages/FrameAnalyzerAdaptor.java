package edu.ubc.mirrors.mirages;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public class FrameAnalyzerAdaptor extends ClassVisitor {

    private final ClassLoader loader;
    private Type thisType = null;
    private Type superType = null;
    private boolean isInterface;
    private List<Type> interfaces;
    
    public FrameAnalyzerAdaptor(ClassLoader loader, ClassVisitor cv) {
        super(Opcodes.ASM4, cv);
        this.loader = loader;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.thisType = Type.getObjectType(name);
        this.isInterface = (Opcodes.ACC_INTERFACE & access) != 0;
        this.superType = superName == null
                ? null
                : Type.getObjectType(superName);
        this.interfaces = new ArrayList<Type>();
        for (String i : interfaces) {
            this.interfaces.add(Type.getObjectType(i));
        }
        
        super.visit(version, access, name, signature, superName, interfaces);
    }
    
    static void printAnalyzerResult(
            MethodNode method,
            Analyzer<FrameValue> a,
            final PrintWriter pw)
    {
        Frame<FrameValue>[] frames = a.getFrames();
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
        return n == -1 ? name : name.substring(n + 1, k);
    }
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        final MethodVisitor superVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        
        final FrameVerifier verifier = new FrameVerifier(thisType, superType, interfaces, isInterface);
        verifier.setClassLoader(loader);
        return new MethodNode(access, name, desc, null, null) {
            @Override
            public void visitEnd() {
                System.out.println("Analyzing: " + name);
                Analyzer<FrameValue> a = new FrameAnalyzer(verifier);
                try {
                    a.analyze(thisType.getInternalName(), this);
                } catch (Exception e) {
                    if (e instanceof IndexOutOfBoundsException
                            && maxLocals == 0 && maxStack == 0)
                    {
                        throw new RuntimeException("Data flow checking option requires valid, non zero maxLocals and maxStack values.");
                    }
                    e.printStackTrace();
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw, true);
                    printAnalyzerResult(this, a, pw);
                    pw.close();
                    throw new RuntimeException(e.getMessage() + ' '
                            + sw.toString());
                }
                
                if (superVisitor != null) {
                    accept(superVisitor);
                }
            }
        };
    }
}
