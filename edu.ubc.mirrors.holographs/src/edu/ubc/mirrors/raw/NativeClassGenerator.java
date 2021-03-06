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
package edu.ubc.mirrors.raw;

import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.util.CheckClassAdapter;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.holograms.HologramClassLoader;

public class NativeClassGenerator extends RemappingClassAdapter {

    public static Remapper REMAPPER = new Remapper() {
        public String map(String typeName) {
            return getNativeInternalClassName(typeName);
        };  
    };
    
    public NativeClassGenerator(ClassVisitor cv) {
        super(cv, REMAPPER);
    }
    
    private String className;
    private String superName;
    private boolean isInterface;
    
    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {

        this.className = getNativeInternalClassName(name);
        this.superName = getNativeInternalClassName(superName);
        this.isInterface = (Opcodes.ACC_INTERFACE & access) != 0;
        
        super.visit(version, access, this.className, signature, this.superName, interfaces);
    }
    
    public static String getNativeBinaryClassName(String className) {
        if (className == null) {
            return null;
        }
        
        return getNativeInternalClassName(className.replace('.', '/')).replace('/', '.');
    }
    public static String getNativeInternalClassName(String className) {
        if (className == null) {
            return null;
        }
        
        if (className.equals(Type.getInternalName(Object.class))) {
            return className;
        }
        
        if (!className.startsWith("native") || className.startsWith("java")) {
            return "native/" + className;
        } else {
            return className;
        }
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        
        return null;
    }
    
    @Override
    public void visitEnd() {
        if (!isInterface) {
            // Generate the no-argument constructor
            String constructorDesc = Type.getMethodDescriptor(Type.VOID_TYPE);
            MethodVisitor methodVisitor = super.visitMethod(Opcodes.ACC_PUBLIC, 
                             "<init>", constructorDesc, null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", constructorDesc);
            methodVisitor.visitInsn(Opcodes.RETURN);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd(); 
        }
        
        super.visitEnd();
    }
    
    public static String getOriginalClassName(String nativeClassName) {
        if (nativeClassName.startsWith("[")) {
            return "[" + getOriginalClassName(nativeClassName.substring(1));
        }
        
        if (nativeClassName.startsWith("native")) {
            return nativeClassName.substring("native".length() + 1);
        } else {
            return nativeClassName;
        }
    }
    
    public static byte[] generate(ClassMirror classMirror) throws IOException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES & ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = classWriter;
        visitor = new CheckClassAdapter(visitor);
        visitor = new NativeClassGenerator(visitor);
        ClassReader reader = new ClassReader(classMirror.getBytecode());
        reader.accept(visitor, ClassReader.SKIP_FRAMES);
        return classWriter.toByteArray();
    }
}
