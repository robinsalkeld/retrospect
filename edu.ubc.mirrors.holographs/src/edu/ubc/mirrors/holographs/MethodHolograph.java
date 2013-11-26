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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.MirrorLocation;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.wrapping.WrappingMethodMirror;

public class MethodHolograph implements MethodMirror {

    private final ClassHolograph klass;
    private final MethodMirror wrapped;
    private MethodMirror bytecodeMethod;
    private Method hologramClassMethod;
    private boolean accessible = false;
    
    public MethodHolograph(ClassHolograph klass, MethodMirror wrapped) {
	this.klass = klass;
        this.wrapped = wrapped;
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        
        return ((MethodHolograph)obj).wrapped.equals(wrapped);
    }
    
    @Override
    public final int hashCode() {
        return 47 * wrapped.hashCode();
    }
    
    private MethodMirror getBytecodeMethod() {
	if (bytecodeMethod == null) {
	    try {
		bytecodeMethod = klass.getBytecodeMirror().getDeclaredMethod(wrapped.getName(), wrapped.getParameterTypeNames().toArray(new String[0]));
	    } catch (NoSuchMethodException e) {
		throw new RuntimeException(e);
	    }
	}
	return bytecodeMethod;
    }
    
    private void resolveMethod() {
        List<ClassMirror> paramTypes = getParameterTypes();
        Class<?>[] hologramParamTypes = new Class<?>[paramTypes.size()];
        for (int i = 0; i < hologramParamTypes.length; i++) {
            hologramParamTypes[i] = ClassHolograph.getHologramClass(paramTypes.get(i), false);
        }
        Class<?> hologramClass = klass.getHologramClass(true);
        // Account for the fact that ObjectHologram is not actually the top of the type lattice
        if (klass.getClassName().equals(Object.class.getName())) {
            hologramClass = Object.class;
        }
        try {
            hologramClassMethod = hologramClass.getDeclaredMethod(getName(), hologramParamTypes);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(getName());
        }
        hologramClassMethod.setAccessible(accessible);
    }
    
    @Override
    public byte[] getBytecode() {
        if (klass.hasBytecode()) {
            return wrapped.getBytecode();
        } else {
            return getBytecodeMethod().getBytecode();
        }
    }
    
    @Override
    public List<AnnotationMirror> getAnnotations(ThreadMirror thread) {
        if (klass.hasBytecode()) {
            return wrapped.getAnnotations(thread);
        } else {
            return getBytecodeMethod().getAnnotations(thread);
        }
    }
    
    @Override
    public Object invoke(ThreadMirror thread, ObjectMirror obj, Object ... args) throws IllegalAccessException, MirrorInvocationTargetException {
        if (thread == null) {
            throw new NullPointerException();
        }
        
        ThreadHolograph threadHolograph = ((ThreadHolograph)thread);
        threadHolograph.enterHologramExecution();
        try {
            resolveMethod();
            
            Object[] hologramArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                hologramArgs[i] = ClassHolograph.makeHologram(args[i]);
            }
            Object hologramObj = ClassHolograph.makeHologram(obj);
            try {
                Object result = hologramClassMethod.invoke(hologramObj, hologramArgs);
                // Account for the fact that toString() has to return a real String here
                if (result instanceof String) {
                    return klass.getVM().makeString((String)result);
                } else {
                    return ClassHolograph.unwrapHologram(result);
                }
            
            } catch (InvocationTargetException e) {
                throw ClassHolograph.causeAsMirrorInvocationTargetException(e);
            }
        } finally {
            threadHolograph.exitHologramExecution();
        }
    }
    
    @Override
    public void setAccessible(boolean flag) {
        if (hologramClassMethod != null) {
            hologramClassMethod.setAccessible(flag);
        } else {
            accessible = flag;
        }
    }
    
    @Override
    public List<String> getParameterTypeNames() {
        return wrapped.getParameterTypeNames();
    }
    
    @Override
    public List<ClassMirror> getParameterTypes() {
        return wrapped.getParameterTypes();
    }
    
    @Override
    public String getReturnTypeName() {
        return wrapped.getReturnTypeName();
    }
    
    @Override
    public ClassMirror getReturnType() {
        return wrapped.getReturnType();
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public List<List<AnnotationMirror>> getParameterAnnotations(ThreadMirror thread) {
        return wrapped.getParameterAnnotations(thread);
    }
    
    @Override
    public Object getDefaultValue(ThreadMirror thread) {
        return wrapped.getDefaultValue(thread);
    }
    
    @Override
    public ClassMirror getDeclaringClass() {
        return klass;
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
        if (klass.hasBytecode()) {
            return wrapped.getExceptionTypeNames();
        } else {
            return getBytecodeMethod().getExceptionTypeNames();
        }
    }
    
    @Override
    public List<ClassMirror> getExceptionTypes() {
        if (klass.hasBytecode()) {
            return wrapped.getExceptionTypes();
	} else {
	    return getBytecodeMethod().getExceptionTypes();
	}
    }

    @Override
    public String getSignature() {
        return wrapped.getSignature();
    }
    
    @Override
    public MirrorLocation locationForBytecodeOffset(int offset) {
        return wrapped.locationForBytecodeOffset(offset);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + wrapped;
    }
}
