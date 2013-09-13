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
package edu.ubc.mirrors.raw.nativestubs.java.util;

import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.holograms.HologramClassLoader;
import edu.ubc.mirrors.holograms.ObjectHologram;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class ResourceBundleStubs extends NativeStubs {

    public ResourceBundleStubs(ClassHolograph klass) {
        super(klass);
    }

    @StubMethod
    public ObjectArrayMirror getClassContext() {
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        
        int nativeDepth = 1;
        Class<?> klass = sun.reflect.Reflection.getCallerClass(nativeDepth);
        while (klass != null) {
            // Filter out any non-holographic frames
            ClassLoader loader = klass.getClassLoader();
            if (loader instanceof HologramClassLoader) {
                result.add(ObjectHologram.getClassMirrorForHolographicClass(klass));
            }
            nativeDepth++;
            klass = sun.reflect.Reflection.getCallerClass(nativeDepth);
        }
        
        ThreadMirror currentThread = ThreadHolograph.currentThreadMirror();
        for (FrameMirror frame : currentThread.getStackTrace()) {
            result.add(frame.declaringClass());
        }
        
        // Remove the frame for the native method
        result.remove(0);
        
        return Reflection.toArray(getVM().findBootstrapClassMirror(Class.class.getName()), result);
    }
    
}
