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

import java.util.List;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;

public class NativeClassMirrorLoader extends NativeInstanceMirror implements ClassMirrorLoader {

    public final ClassLoader classLoader;
    
    public NativeClassMirrorLoader(ClassLoader classLoader) {
        super(classLoader);
        this.classLoader = classLoader;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NativeClassMirrorLoader)) {
            return false;
        }
        
        NativeClassMirrorLoader other = (NativeClassMirrorLoader)obj;
        return classLoader == null ? other.classLoader == null : classLoader.equals(other.classLoader);
    }
    
    @Override
    public int hashCode() {
        return 17 + classLoader.hashCode();
    }
    
    @Override
    public List<ClassMirror> loadedClassMirrors() {
        // TODO-RS: Bah, no time...
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ClassMirror findLoadedClassMirror(String name) {
        try {
            // TODO-RS: Do we need to call the native findLoadedClass0() method directly?
            // Is this shortcut harmful?
            Class<?> klass = Class.forName(name, false, classLoader);
            if (klass.getClassLoader() == classLoader) {
                return new NativeClassMirror(klass);
            } else {
                return null;
            }
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    
    @Override
    public ClassMirror defineClass(String name, ByteArrayMirror b, int off,
            int len, InstanceMirror pd, InstanceMirror source, boolean unsafe) {
        // TODO-RS: Bah, no time...
        throw new UnsupportedOperationException();
    }
}
