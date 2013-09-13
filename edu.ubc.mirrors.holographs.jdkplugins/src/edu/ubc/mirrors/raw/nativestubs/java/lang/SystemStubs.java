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

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class SystemStubs extends NativeStubs {

    public SystemStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public int identityHashCode(ObjectMirror o) {
        return o.identityHashCode();
    }
    
    @StubMethod
    public void arraycopy(ObjectMirror src, int srcPos, ObjectMirror dest, int destPos, int length) {
        Reflection.arraycopy(src, srcPos, dest, destPos, length);
    }
    
    @StubMethod
    public void setIn0(InstanceMirror stream) throws IllegalAccessException, NoSuchFieldException {
        ClassMirror systemClass = getVM().findBootstrapClassMirror(System.class.getName());
        systemClass.getStaticFieldValues().set(systemClass.getDeclaredField("in"), stream);
    }
    
    @StubMethod
    public void setOut0(InstanceMirror stream) throws IllegalAccessException, NoSuchFieldException {
        ClassMirror systemClass = getVM().findBootstrapClassMirror(System.class.getName());
        systemClass.getStaticFieldValues().set(systemClass.getDeclaredField("out"), stream);
    }
    
    @StubMethod
    public void setErr0(InstanceMirror stream) throws IllegalAccessException, NoSuchFieldException {
        ClassMirror systemClass = getVM().findBootstrapClassMirror(System.class.getName());
        systemClass.getStaticFieldValues().set(systemClass.getDeclaredField("err"), stream);
    }
    
    // TODO-RS: I don't like this as a general rule, but it's called from 
    // ClassLoader#defineClass() in JDK 7 to measure loading time,
    // and also seems necessary in the read-only mapped fs.
    // It's probably actually okay, because this will just look like a very long
    // system delay to the original process, which is fairly reasonable.
    @StubMethod
    public long nanoTime() {
        return System.nanoTime();
    }
    
    @StubMethod
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
    
    @StubMethod
    public InstanceMirror mapLibraryName(InstanceMirror libnameMirror) {
        // TODO-RS: Switch based on supported platforms.
        String libname = Reflection.getRealStringForMirror(libnameMirror);
        String mappedLibname = libname + ".so";
        return Reflection.makeString(getVM(), mappedLibname);
    }
}
