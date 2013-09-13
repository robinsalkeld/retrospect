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

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;

import edu.ubc.mirrors.holograms.MainEntryAdaptor;
import edu.ubc.mirrors.holograms.HologramClassLoader;
import edu.ubc.mirrors.holograms.ObjectHologramAgent;

public class ObjectHologramAgent implements ClassFileTransformer {

    public static void premain(String options, Instrumentation instr) {
        for (Class<?> c : instr.getAllLoadedClasses()) {
            System.out.println(c.getName());
        }
        instr.addTransformer(new ObjectHologramAgent());
    }

    public byte[] transform(ClassLoader loader, String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {
        
        try {
            String binaryName = className.replace('/', '.');
//            System.out.println(binaryName);
            if (binaryName.equals(HologramClassLoader.class.getName())) return null;
//            Thread.dumpStack();
            // TODO: Enable once I work out how to ensure classes are resolved in the same order
//            ObjectHologram.defineHologramClass(binaryName, loader, new ClassReader(classfileBuffer));
            
            if (!(loader instanceof HologramClassLoader)) {
                return MainEntryAdaptor.generate(binaryName, new ClassReader(classfileBuffer), null);
            } else {
                return null;
            }
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            System.exit(-1);
            return null;
        }
    }
}
