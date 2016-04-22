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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ThreadMirror;

public class ConstructorHolograph implements ConstructorMirror {

    private final ClassHolograph klass;
    private final ConstructorMirror wrapped;
    private ConstructorMirror bytecodeConstructor;
    private Constructor<?> hologramClassConstructor;
    private static final Method NEW_INSTANCE_METHOD;
    static {
        try {
            NEW_INSTANCE_METHOD = Constructor.class.getDeclaredMethod("newInstance", Object[].class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }
    
    public ConstructorHolograph(ClassHolograph klass, ConstructorMirror wrapped) {
	this.klass = klass;
        this.wrapped = wrapped;
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        
        return ((ConstructorHolograph)obj).wrapped.equals(wrapped);
    }
    
    @Override
    public final int hashCode() {
        return 47 * wrapped.hashCode();
    }
    
    private ConstructorMirror getBytecodeConstructor() {
	if (bytecodeConstructor == null) {
	    try {
		bytecodeConstructor = klass.getBytecodeMirror().getConstructor(wrapped.getParameterTypeNames().toArray(new String[0]));
	    } catch (NoSuchMethodException e) {
		throw new RuntimeException(e);
	    }
	}
	return bytecodeConstructor;
    }
    
    private void resolveConstructor() {
        List<ClassMirror> paramTypes = getParameterTypes();
        // Add the extra implicit mirror parameter
        Class<?>[] hologramParamTypes = new Class<?>[paramTypes.size() + 1];
        for (int i = 0; i < hologramParamTypes.length - 1; i++) {
            hologramParamTypes[i] = ClassHolograph.getHologramClass(paramTypes.get(i), false);
        }
        hologramParamTypes[hologramParamTypes.length - 1] = InstanceMirror.class;
        
        Class<?> hologramClass = ClassHolograph.getHologramClass(klass, true);
        
        try {
            hologramClassConstructor = hologramClass.getDeclaredConstructor(hologramParamTypes);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError();
        }
        hologramClassConstructor.setAccessible(true);
    }
    
    @Override
    public InstanceMirror newInstance(final ThreadMirror thread, final Object ... args) throws IllegalAccessException, MirrorInvocationTargetException {
        final ThreadHolograph threadHolograph = ((ThreadHolograph)thread);
        try {
            return threadHolograph.withHologramExecution(new Callable<InstanceMirror>() {
                public InstanceMirror call() throws Exception {
                    try {
                        klass.ensureInitialized();
                        
                        resolveConstructor();
                        
                        // Add the extra implicit mirror parameter
                        InstanceMirror mirror = getDeclaringClass().newRawInstance();
                        Object[] hologramArgs = new Object[args.length + 1];
                        Class<?>[] paramTypes = hologramClassConstructor.getParameterTypes(); 
                        for (int i = 0; i < args.length; i++) {
                            hologramArgs[i] = ClassHolograph.makeHologram(thread, paramTypes[i], args[i]);
                        }
                        hologramArgs[args.length] = mirror;
                        
                        Object result = threadHolograph.getHologramThread().invoke(hologramClassConstructor, NEW_INSTANCE_METHOD, new Object[]{hologramArgs});
                        return (InstanceMirror)ClassHolograph.unwrapHologram(result);
                    } catch (InvocationTargetException e) {
                        throw ClassHolograph.causeAsMirrorInvocationTargetException(e);
                    }
                }
            });
        } catch (IllegalAccessException e) {
            throw e;
        } catch (MirrorInvocationTargetException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
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
    public List<AnnotationMirror> getAnnotations(ThreadMirror thread) {
        try {
            return wrapped.getAnnotations(thread);
        } catch (UnsupportedOperationException e) {
            return getBytecodeConstructor().getAnnotations(thread);
        }
    }

    @Override
    public List<List<AnnotationMirror>> getParameterAnnotations(ThreadMirror thread) {
        try {
            return wrapped.getParameterAnnotations(thread);
        } catch (UnsupportedOperationException e) {
            return getBytecodeConstructor().getParameterAnnotations(thread);
        }
    }

    @Override
    public ClassMirror getDeclaringClass() {
        return klass;
    }

    @Override
    public int getModifiers() {
        try {
            return wrapped.getModifiers();
        } catch (UnsupportedOperationException e) {
            return getBytecodeConstructor().getModifiers();
        }
    }

    @Override
    public List<String> getExceptionTypeNames() {
        try {
            return wrapped.getExceptionTypeNames();
        } catch (UnsupportedOperationException e) {
            return getBytecodeConstructor().getExceptionTypeNames();
        }
    }
    
    @Override
    public List<ClassMirror> getExceptionTypes() {
        try {
	    return wrapped.getExceptionTypes();
	} catch (UnsupportedOperationException e) {
	    return getBytecodeConstructor().getExceptionTypes();
	}
    }

    @Override
    public String getSignature() {
        return wrapped.getSignature();
    }
    
    public ConstructorMirror getWrapped() {
        return wrapped;
    }
}
