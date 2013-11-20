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
package edu.ubc.mirrors.raw.nativestubs.java.lang.reflect;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;
import edu.ubc.mirrors.raw.nativestubs.java.lang.ClassLoaderStubs;

public class ProxyStubs extends NativeStubs {

    public ProxyStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public ClassMirror defineClass0(ClassMirrorLoader loader, InstanceMirror name, ByteArrayMirror b, int off, int len) {
        String realClassName = Reflection.getRealStringForMirror(name);
        
        if (loader == null) {
            return klass.getVM().defineBootstrapClass(realClassName, b, off, len);
        } else {
            InstanceMirror className = klass.getVM().makeString(realClassName);
            return ClassLoaderStubs.defineClass(loader, className, b, off, len, null, null, false);
        }
    }
}
