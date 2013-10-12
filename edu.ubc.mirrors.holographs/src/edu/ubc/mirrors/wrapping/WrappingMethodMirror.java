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
package edu.ubc.mirrors.wrapping;

import java.util.List;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;

public class WrappingMethodMirror implements MethodMirror {

    private final WrappingVirtualMachine vm;
    protected final MethodMirror wrapped;
    
    public WrappingMethodMirror(WrappingVirtualMachine vm, MethodMirror wrapped) {
        this.vm = vm;
        this.wrapped = wrapped;
    }
    
    @Override
    public Object invoke(ThreadMirror thread, ObjectMirror obj, Object... args)
            throws IllegalArgumentException, IllegalAccessException,
            MirrorInvocationTargetException {

        Object[] unwrappedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            unwrappedArgs[i] = vm.unwrappedValue(args[i]);
        }
        ObjectMirror unwrappedObj = vm.unwrapMirror(obj);
        ThreadMirror unwrappedThread = (ThreadMirror)vm.unwrapMirror(thread);
        Object result;
        try {
            result = wrapped.invoke(unwrappedThread, unwrappedObj, unwrappedArgs);
        } catch (MirrorInvocationTargetException e) {
            throw new MirrorInvocationTargetException((InstanceMirror)vm.getWrappedMirror(e.getTargetException()));
        }
        return vm.wrapValue(result);
    }
    
    @Override
    public void setAccessible(boolean flag) {
        wrapped.setAccessible(flag);
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }
    
    @Override
    public List<String> getParameterTypeNames() {
        return wrapped.getParameterTypeNames();
    }
    
    @Override
    public List<ClassMirror> getParameterTypes() {
        return vm.getWrappedClassMirrorList(wrapped.getParameterTypes());
    }

    @Override
    public String getReturnTypeName() {
        return wrapped.getReturnTypeName();
    }
    
    @Override
    public ClassMirror getReturnType() {
        return vm.getWrappedClassMirror(wrapped.getReturnType());
    }
    
    @Override
    public List<AnnotationMirror> getAnnotations() {
        return vm.wrapAnnotations(wrapped.getAnnotations());
    }
    
    @Override
    public byte[] getRawAnnotations() {
        return wrapped.getRawAnnotations();
    }

    @Override
    public byte[] getRawParameterAnnotations() {
        return wrapped.getRawParameterAnnotations();
    }

    @Override
    public byte[] getRawAnnotationDefault() {
        return wrapped.getRawAnnotationDefault();
    }

    @Override
    public ClassMirror getDeclaringClass() {
        return vm.getWrappedClassMirror(wrapped.getDeclaringClass());
    }

    @Override
    public int getSlot() {
        return wrapped.getSlot();
    }

    @Override
    public int getModifiers() {
        return wrapped.getModifiers();
    }

    @Override
    public List<String> getExceptionTypeNames() {
        return wrapped.getExceptionTypeNames();
    }
    
    @Override
    public List<ClassMirror> getExceptionTypes() {
        return vm.getWrappedClassMirrorList(wrapped.getExceptionTypes());
    }

    @Override
    public String getSignature() {
        // TODO-RS: Probably need to parse and re-build this too
        return wrapped.getSignature();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + wrapped;
    }
}
