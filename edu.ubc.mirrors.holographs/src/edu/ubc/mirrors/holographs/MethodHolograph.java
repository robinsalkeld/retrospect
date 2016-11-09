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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.MirrorLocation;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.holograms.MethodHologram;

public class MethodHolograph implements MethodMirror {

    private final ClassHolograph klass;
    final MethodMirror wrapped;
    private MethodMirror bytecodeMethod;
    private MethodHologram hologramMethod;
    
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
        if (hologramMethod == null) {
            hologramMethod = MethodHologram.make(klass, getName(), getParameterTypes());
        }
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
        try {
            return wrapped.getAnnotations(thread);
        } catch (UnsupportedOperationException e) {
            return getBytecodeMethod().getAnnotations(thread);
        }
    }
    
    @Override
    public Object invoke(ThreadMirror thread, final ObjectMirror obj, final Object ... args) throws IllegalAccessException, MirrorInvocationTargetException {
        final ThreadHolograph threadHolograph = ((ThreadHolograph)thread);
        try {
            return threadHolograph.withHologramExecution(new Callable<Object>() {
                public Object call() throws Exception {
                    resolveMethod();
                    
                    List<Object> combinedArgs = new ArrayList<Object>(Arrays.asList(args));
                    if ((getModifiers() & Modifier.STATIC) == 0) {
                        combinedArgs.add(0, obj);
                    }
                    return klass.getVM().eventRequestManager().handleAdvice(threadHolograph, hologramMethod, combinedArgs);
                }
            });
        } catch (IllegalAccessException e) {
            throw e;
        } catch (MirrorInvocationTargetException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
    
    @Override
    public void setAccessible(boolean flag) {
        // Ignore
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
        try {
            return wrapped.getParameterAnnotations(thread);
        } catch (UnsupportedOperationException e) {
            return getBytecodeMethod().getParameterAnnotations(thread);
        }
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
    public int getModifiers() {
        try {
            return wrapped.getModifiers();
        } catch (UnsupportedOperationException e) {
            return getBytecodeMethod().getModifiers();
        }
    }

    @Override
    public List<String> getExceptionTypeNames() {
        try {
            return wrapped.getExceptionTypeNames();
        } catch (UnsupportedOperationException e) {
            return getBytecodeMethod().getExceptionTypeNames();
        }
    }
    
    @Override
    public List<ClassMirror> getExceptionTypes() {
        try {
            return wrapped.getExceptionTypes();
	} catch (UnsupportedOperationException e) {
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
