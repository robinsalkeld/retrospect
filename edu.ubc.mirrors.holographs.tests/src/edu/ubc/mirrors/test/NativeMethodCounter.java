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
package edu.ubc.mirrors.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.MethodNode;

public class NativeMethodCounter extends ClassVisitor {

    public NativeMethodCounter() {
        super(Opcodes.ASM4);
    }

    int nativeMethodCount = 0;
    String currentClass = null;
    SortedMap<String, Set<MethodNode>> classesWithNativeMethods = new TreeMap<String, Set<MethodNode>>();
    
    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        currentClass = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        
        if ((access & Opcodes.ACC_NATIVE) != 0) {
            nativeMethodCount++;
            Set<MethodNode> methods = classesWithNativeMethods.get(currentClass);
            if (methods == null) {
                methods = new HashSet<MethodNode>();
                classesWithNativeMethods.put(currentClass, methods);
            }
            methods.add(new MethodNode(access, name, desc, signature, exceptions));
        }

        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}
