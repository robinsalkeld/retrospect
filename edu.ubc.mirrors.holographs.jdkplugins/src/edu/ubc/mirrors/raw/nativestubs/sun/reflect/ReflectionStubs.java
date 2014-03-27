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
package edu.ubc.mirrors.raw.nativestubs.sun.reflect;

import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.holograms.HologramClassGenerator;
import edu.ubc.mirrors.holograms.HologramClassLoader;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class ReflectionStubs extends NativeStubs {

    public ReflectionStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public static ClassMirror getCallerClassMirror(int depth) {
        // Filter out any non-holographic frames
        int nativeDepth = 1;
        Class<?> klass = sun.reflect.Reflection.getCallerClass(nativeDepth);
        while (klass != null) {
            ClassLoader loader = klass.getClassLoader();
            if (loader instanceof HologramClassLoader) {
                if (depth == 0) {
                    HologramClassLoader hologramClassLoader = (HologramClassLoader)klass.getClassLoader();
                    String className = HologramClassGenerator.getOriginalBinaryClassName(klass.getName());
                    return hologramClassLoader.loadOriginalClassMirror(className);
                }
                depth--;
            }
            nativeDepth++;
            klass = sun.reflect.Reflection.getCallerClass(nativeDepth);
        }
        
        // Off the top of the holographic stack, so refer to the original stack.
        ThreadMirror currentThread = ThreadHolograph.currentThreadMirror();
        List<FrameMirror> stack = currentThread.getStackTrace();
        int frameIndex = stack.size() - 1 - depth;
        if (frameIndex < 0) {
            return null;
        }
        FrameMirror frame = stack.get(frameIndex);
        return frame.declaringClass();
    }
    
    @StubMethod
    public ClassMirror getCallerClass(int depth) {
        return getCallerClassMirror(depth);
    }
    
    @StubMethod
    public ClassMirror getCallerClass() {
        // 0 = Reflection, 1 = caller of this method, 2 = desired caller
        return getCallerClassMirror(2);
    }
    
    @StubMethod
    public int getClassAccessFlags(ClassMirror klass) {
        return klass.getModifiers();
    }
}
