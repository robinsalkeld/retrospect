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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.asm.Opcodes;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.holographs.MirrorInvocationHandler;

public class NativeStubsInvocationHandler implements MirrorInvocationHandler {

    private final Object stubsClassInstance;
    private final Method stubsMethod;
    
    public NativeStubsInvocationHandler(Object stubsClassInstance, Method stubsMethod) {
        this.stubsClassInstance = stubsClassInstance;
        this.stubsMethod = stubsMethod;
    }

    @Override
    public Object invoke(InstanceMirror object, MethodMirror method, Object[] args) throws MirrorInvocationTargetException {
        Object[] stubsArgs = args;
        if ((Opcodes.ACC_STATIC & method.getModifiers()) == 0) {
            stubsArgs = new Object[args.length + 1];
            stubsArgs[0] = object;
            System.arraycopy(args, 0, stubsArgs, 1, args.length);
        }
        try {
            return stubsMethod.invoke(stubsClassInstance, stubsArgs);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof MirrorInvocationTargetException) {
                throw (MirrorInvocationTargetException)e.getCause();
            }
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
