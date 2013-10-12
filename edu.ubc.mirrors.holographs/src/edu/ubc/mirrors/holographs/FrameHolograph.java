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
package edu.ubc.mirrors.holographs;

import java.util.List;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.wrapping.WrappingFrameMirror;
import edu.ubc.mirrors.wrapping.WrappingVirtualMachine;

public class FrameHolograph extends WrappingFrameMirror {

    public FrameHolograph(WrappingVirtualMachine vm, FrameMirror wrapped) {
        super(vm, wrapped);
    }

    private MethodMirror lazyMethod;
    
    @Override
    public MethodMirror method() {
        // TODO-RS: Not quite the right check...
        if (vm.getWrappedVM().canGetBytecodes()) {
            return super.method();
        } else {
            if (lazyMethod == null) {
                lazyMethod = new LazyMethodMirror();
            }
            return lazyMethod;
        }
    }
    
    // Lazy proxy so we don't trigger errors (if the bytecode is missing, for example)
    // on just grabbing the name and declaring class.
    private class LazyMethodMirror implements MethodMirror {
     
        private MethodMirror wrapped;
        
        /**
         * @return
         * @see edu.ubc.mirrors.MethodMirror#getDeclaringClass()
         */
        public ClassMirror getDeclaringClass() {
            return FrameHolograph.this.declaringClass();
        }

        /**
         * @return
         * @see edu.ubc.mirrors.MethodMirror#getName()
         */
        public String getName() {
            return FrameHolograph.this.methodName();
        }

        @Override
        public List<AnnotationMirror> getAnnotations() {
            resolve();
            return wrapped.getAnnotations();
        }
        
        /**
         * @return
         * @see edu.ubc.mirrors.MethodMirror#getSlot()
         */
        public int getSlot() {
            resolve();
            return wrapped.getSlot();
        }

        /**
         * @return
         * @see edu.ubc.mirrors.MethodMirror#getModifiers()
         */
        public int getModifiers() {
            resolve();
            return wrapped.getModifiers();
        }

        /**
         * @return
         * @see edu.ubc.mirrors.MethodMirror#getParameterTypeNames()
         */
        public List<String> getParameterTypeNames() {
            resolve();
            return wrapped.getParameterTypeNames();
        }

        /**
         * @return
         * @see edu.ubc.mirrors.MethodMirror#getParameterTypes()
         */
        public List<ClassMirror> getParameterTypes() {
            resolve();
            return wrapped.getParameterTypes();
        }

        /**
         * @return
         * @see edu.ubc.mirrors.MethodMirror#getExceptionTypeNames()
         */
        public List<String> getExceptionTypeNames() {
            resolve();
            return wrapped.getExceptionTypeNames();
        }

        /**
         * @return
         * @see edu.ubc.mirrors.MethodMirror#getExceptionTypes()
         */
        public List<ClassMirror> getExceptionTypes() {
            resolve();
            return wrapped.getExceptionTypes();
        }

        /**
         * @return
         * @see edu.ubc.mirrors.MethodMirror#getReturnTypeName()
         */
        public String getReturnTypeName() {
            resolve();
            return wrapped.getReturnTypeName();
        }

        /**
         * @return
         * @see edu.ubc.mirrors.MethodMirror#getReturnType()
         */
        public ClassMirror getReturnType() {
            resolve();
            return wrapped.getReturnType();
        }

        /**
         * @return
         * @see edu.ubc.mirrors.MethodMirror#getSignature()
         */
        public String getSignature() {
            resolve();
            return wrapped.getSignature();
        }

        /**
         * @param thread
         * @param obj
         * @param args
         * @return
         * @throws IllegalArgumentException
         * @throws IllegalAccessException
         * @throws MirrorInvocationTargetException
         * @see edu.ubc.mirrors.MethodMirror#invoke(edu.ubc.mirrors.ThreadMirror, edu.ubc.mirrors.ObjectMirror, java.lang.Object[])
         */
        public Object invoke(ThreadMirror thread, ObjectMirror obj,
                Object... args) throws IllegalArgumentException,
                IllegalAccessException, MirrorInvocationTargetException {
            resolve();
            return wrapped.invoke(thread, obj, args);
        }

        /**
         * @param flag
         * @see edu.ubc.mirrors.MethodMirror#setAccessible(boolean)
         */
        public void setAccessible(boolean flag) {
            resolve();
            wrapped.setAccessible(flag);
        }

        /**
         * @return
         * @see edu.ubc.mirrors.MethodMirror#getRawAnnotations()
         */
        public byte[] getRawAnnotations() {
            resolve();
            return wrapped.getRawAnnotations();
        }

        /**
         * @return
         * @see edu.ubc.mirrors.MethodMirror#getRawParameterAnnotations()
         */
        public byte[] getRawParameterAnnotations() {
            resolve();
            return wrapped.getRawParameterAnnotations();
        }

        /**
         * @return
         * @see edu.ubc.mirrors.MethodMirror#getRawAnnotationDefault()
         */
        public byte[] getRawAnnotationDefault() {
            resolve();
            return wrapped.getRawAnnotationDefault();
        }

        private void resolve() {
            if (wrapped != null) {
                return;
            }
            
            String methodName = methodName();
            for (MethodMirror method : declaringClass().getDeclaredMethods(false)) {
                // TODO-RS: Need to figure out a way to disambiguate, probably using line numbers?
                if (method.getName().equals(methodName)) {
                    wrapped = method;
                    return;
                }
            }
            throw new InternalError("Can't locate method: " + declaringClass().getClassName() + "#" + methodName);
        }
    }
}
