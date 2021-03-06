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

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import edu.ubc.mirrors.holograms.ObjectHologram;

public class MainEntryAdaptor extends ClassVisitor {

    public MainEntryAdaptor(ClassVisitor output) {
        super(Opcodes.ASM4, output);
    }
    
    private String className;
    
    private static final String mainDesc = Type.getMethodDescriptor(
            Type.getType(Void.TYPE), 
            Type.getType(String[].class));
    
    private static final String invokeHologramMainMethodDesc = Type.getMethodDescriptor(
            Type.getType(Void.TYPE), 
            Type.getType(Class.class), Type.getType(String[].class));
    
    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        
        this.className = name;
        
        super.visit(version, access, name, signature, superName, interfaces);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        
        MethodVisitor superVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (superVisitor != null && name.equals("main") && (Opcodes.ACC_STATIC & access) != 0 && desc.equals(mainDesc)) {
            superVisitor.visitCode();
            superVisitor.visitLdcInsn(Type.getObjectType(className));
            superVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            superVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ObjectHologram.class), "invokeHologramMainMethod", invokeHologramMainMethodDesc);
            superVisitor.visitInsn(Opcodes.RETURN);
            superVisitor.visitMaxs(2, 1);
            superVisitor.visitEnd();
            return null;
        } else {
            return superVisitor;
        }
    }
    
    public static byte[] generate(String className, ClassReader reader, String traceDir) throws FileNotFoundException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES & ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = classWriter;
        if (traceDir != null) {
            visitor = new TraceClassVisitor(visitor, new PrintWriter(traceDir + className + ".txt"));
        }
        visitor = new CheckClassAdapter(visitor);
        visitor = new MainEntryAdaptor(visitor);
        reader.accept(visitor, ClassReader.SKIP_FRAMES);
        return classWriter.toByteArray();
    }
    
    
}
