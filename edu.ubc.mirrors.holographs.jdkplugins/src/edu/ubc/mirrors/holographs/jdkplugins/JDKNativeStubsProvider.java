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
package edu.ubc.mirrors.holographs.jdkplugins;

import java.util.HashMap;
import java.util.Map;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.holographs.MirrorInvocationHandler;
import edu.ubc.mirrors.holographs.MirrorInvocationHandlerProvider;

public class JDKNativeStubsProvider implements MirrorInvocationHandlerProvider {

    // TODO-RS: To implement for the Eclipse example:
    // java.io.RandomAccessFile#readBytes([BII)I
    // java.io.RandomAccessFile#seek(J)V
    // java.lang.Class#getEnclosingMethod0()[Ljava/lang/Object;
    // java.util.ResourceBundle#getClassContext()[Ljava/lang/Class;
    
    private final Map<ClassMirror, MirrorInvocationHandlerProvider> providersByClass 
        = new HashMap<ClassMirror, MirrorInvocationHandlerProvider>();
    
    private MirrorInvocationHandlerProvider getProvider(ClassMirror classMirror) {
        MirrorInvocationHandlerProvider result = providersByClass.get(classMirror);
        if (result != null) {
            return result;
        }
        
        String nativeStubsName = "edu.ubc.mirrors.raw.nativestubs." + classMirror.getClassName() + "Stubs";
        try {
            Class<?> stubsClass = Class.forName(nativeStubsName);
            result = new NativeStubsProvider(classMirror, stubsClass);
            providersByClass.put(classMirror, result);
            return result;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    
    @Override
    public MirrorInvocationHandler getInvocationHandler(MethodMirror method) {
        MirrorInvocationHandlerProvider provider = getProvider(method.getDeclaringClass());
        return provider != null ? provider.getInvocationHandler(method) : null;
    }
}
