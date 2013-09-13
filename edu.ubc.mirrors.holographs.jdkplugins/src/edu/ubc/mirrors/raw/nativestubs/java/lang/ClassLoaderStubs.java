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
package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;
import edu.ubc.mirrors.raw.nativestubs.sun.reflect.ReflectionStubs;

public class ClassLoaderStubs extends NativeStubs {
    
    public ClassLoaderStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public ClassMirror findLoadedClass0(ClassMirrorLoader classLoader, InstanceMirror name) {
        String realName = Reflection.getRealStringForMirror(name);
        return classLoader.findLoadedClassMirror(realName);
    }
    
    @StubMethod
    public ClassMirror findBootstrapClass(ClassMirrorLoader classLoader, InstanceMirror name) {
        String realName = Reflection.getRealStringForMirror(name);
        return klass.getVM().findBootstrapClassMirror(realName);
    }
    
    // JDK 6 version
    @StubMethod
    public ClassMirror defineClass1(ClassMirrorLoader classLoader, InstanceMirror name, ByteArrayMirror b, int off, int len,
            InstanceMirror pd, InstanceMirror source, boolean verify) {
        return defineClass1(classLoader, name, b, off, len, pd, source);
    }
    
    public static ClassMirror defineClass(ClassMirrorLoader classLoader, InstanceMirror name, ByteArrayMirror b, int off, int len,
            InstanceMirror pd, InstanceMirror source, boolean unsafe) {
	
        String realName = Reflection.getRealStringForMirror(name);
        
        return classLoader.defineClass(realName, b, off, len, pd, source, unsafe);
    }
    
    // JDK 7 version
    @StubMethod
    public ClassMirror defineClass1(ClassMirrorLoader classLoader, InstanceMirror name, ByteArrayMirror b, int off, int len,
            InstanceMirror pd, InstanceMirror source) {
        
        return defineClass(classLoader, name, b, off, len, pd, source, false);
    }
    
    // Note: the generic return type Class<? extends ClassLoader> of this method seems broken: it implies the result should
    // always extend ClassLoader but I don't see how that's true. Alternatively it could mean "loader of the class at the given depth"
    // but then the method wouldn't have the correct semantics for the one place it's called.
    @StubMethod
    public ClassMirror getCaller(int depth) {
        return ReflectionStubs.getCallerClassMirror(depth);
    }
}
