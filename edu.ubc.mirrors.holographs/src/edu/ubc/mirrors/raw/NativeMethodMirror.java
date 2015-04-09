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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.MirrorLocation;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;

public class NativeMethodMirror implements MethodMirror {

    private final Method nativeMethod;
    
    public NativeMethodMirror(Method nativeMethod) {
        this.nativeMethod = nativeMethod;
    }
    
    @Override
    public byte[] getBytecode() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Object invoke(ThreadMirror thread, ObjectMirror obj, Object... args)
            throws IllegalArgumentException, IllegalAccessException,
            MirrorInvocationTargetException {

        if (!thread.equals(NativeInstanceMirror.makeMirror(Thread.currentThread()))) {
            throw new IllegalThreadStateException("The native VM can only invoke methods on the current thread");
        }
        
        Object[] nativeArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            nativeArgs[i] = getNativeObject(args[i]);
        }
        Object nativeObj = getNativeObject(obj);
        Object result;
        try {
            result = nativeMethod.invoke(nativeObj, nativeArgs);
        } catch (InvocationTargetException e) {
            throw new MirrorInvocationTargetException((InstanceMirror)NativeInstanceMirror.makeMirror(e.getCause()));
        }
        return wrapNativeObject(result);
    }

    static Object getNativeObject(Object obj) {
        if (obj instanceof NativeInstanceMirror) {
            return ((NativeInstanceMirror)obj).getNativeObject();
        } else {
            return obj;
        }
    }
    
    static Object wrapNativeObject(Object obj) {
        if (obj instanceof NativeInstanceMirror) {
            return ((NativeInstanceMirror)obj).getNativeObject();
        } else {
            return obj;
        }
    }
    
    @Override
    public void setAccessible(boolean flag) {
        nativeMethod.setAccessible(flag);
    }

    @Override
    public String getName() {
        return nativeMethod.getName();
    }

    @Override
    public List<String> getParameterTypeNames() {
        List<String> result = new ArrayList<String>();
        for (Class<?> klass : nativeMethod.getParameterTypes()) {
            result.add(klass.getName());
        }
        return result;
    }
    
    @Override
    public List<ClassMirror> getParameterTypes() {
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        for (Class<?> klass : nativeMethod.getParameterTypes()) {
            result.add(new NativeClassMirror(klass));
        }
        return result;
    }

    @Override
    public String getReturnTypeName() {
        return nativeMethod.getReturnType().getName();
    }
    
    @Override
    public ClassMirror getReturnType() {
        return new NativeClassMirror(nativeMethod.getReturnType());
    }
    
    @Override
    public List<AnnotationMirror> getAnnotations(ThreadMirror thread) {
        return NativeVirtualMachineMirror.makeAnnotations(Arrays.asList(nativeMethod.getAnnotations()));
    }

    @Override
    public List<List<AnnotationMirror>> getParameterAnnotations(ThreadMirror thread) {
        List<List<AnnotationMirror>> result = new ArrayList<List<AnnotationMirror>>();
        for (Annotation[] annotations : nativeMethod.getParameterAnnotations()) {
            result.add(NativeVirtualMachineMirror.makeAnnotations(Arrays.asList(annotations)));
        }
        return result;
    }
    
    @Override
    public Object getDefaultValue(ThreadMirror thread) {
        // TODO For now
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassMirror getDeclaringClass() {
        // TODO For now
        throw new UnsupportedOperationException();
    }

    @Override
    public int getModifiers() {
        // TODO For now
        throw new UnsupportedOperationException();
    }
    
    @Override
    public List<String> getExceptionTypeNames() {
        // TODO For now
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ClassMirror> getExceptionTypes() {
        // TODO For now
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSignature() {
        // TODO For now
        throw new UnsupportedOperationException();
    }
    
    @Override
    public MirrorLocation locationForBytecodeOffset(int offset) {
        throw new UnsupportedOperationException();
    }
    
}
