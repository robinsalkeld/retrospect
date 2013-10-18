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

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class NativeConstructorAccessorImplStubs extends NativeStubs {

    public NativeConstructorAccessorImplStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public ObjectMirror newInstance0(InstanceMirror constructor, ObjectArrayMirror argsMirror) throws IllegalArgumentException, InstantiationException, IllegalAccessException, MirrorInvocationTargetException {
        
        ClassMirror declaringClass = (ClassMirror)HolographInternalUtils.getField(constructor, "clazz");
        ObjectArrayMirror parameterTypesMirror = (ObjectArrayMirror)HolographInternalUtils.getField(constructor, "parameterTypes");
        String[] parameterTypeNames = new String[parameterTypesMirror.length()];
        for (int i = 0; i < parameterTypeNames.length; i++) {
            parameterTypeNames[i] = ((ClassMirror)parameterTypesMirror.get(i)).getClassName();
        }

        ConstructorMirror consMirror;
        try {
            consMirror = declaringClass.getConstructor(parameterTypeNames);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
        
        Object[] argsArray = new Object[argsMirror == null ? 0 : argsMirror.length()];
        if (argsMirror != null) {
            for (int i = 0; i < argsArray.length; i++) {
                argsArray[i] = argsMirror.get(i);
            }
        }
        return consMirror.newInstance(ThreadHolograph.currentThreadMirror(), argsArray);
    }
    
}
