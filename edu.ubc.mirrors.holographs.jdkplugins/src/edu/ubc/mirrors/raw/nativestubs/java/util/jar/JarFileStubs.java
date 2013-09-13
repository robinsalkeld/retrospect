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
package edu.ubc.mirrors.raw.nativestubs.java.util.jar;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.jar.JarFile;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class JarFileStubs extends NativeStubs {
    
    public JarFileStubs(ClassHolograph klass) {
	super(klass);
    }

    private static final Method getMetaInfEntryNamesMethod;
    static {
        try {
            getMetaInfEntryNamesMethod = JarFile.class.getDeclaredMethod("getMetaInfEntryNames");
            getMetaInfEntryNamesMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    
    @StubMethod
    public ObjectArrayMirror getMetaInfEntryNames(InstanceMirror jarFileMirror) throws IllegalAccessException, NoSuchFieldException, IllegalArgumentException, InvocationTargetException {
        VirtualMachineHolograph vm = getVM();
        
        long jzfile = jarFileMirror.getLong(klass.getSuperClassMirror().getDeclaredField("jzfile"));
        JarFile hostJarFile = (JarFile)getVM().getZipFileForAddress(jzfile);
        
        String[] result = (String[]) getMetaInfEntryNamesMethod.invoke(hostJarFile);
        
        ObjectArrayMirror resultMirror = (ObjectArrayMirror)vm.findBootstrapClassMirror(String.class.getName()).newArray(result.length);
        for (int i = 0; i < result.length; i++) {
            resultMirror.set(i, Reflection.makeString(vm, result[i]));
        }
        
        return resultMirror;
    }
    

}
