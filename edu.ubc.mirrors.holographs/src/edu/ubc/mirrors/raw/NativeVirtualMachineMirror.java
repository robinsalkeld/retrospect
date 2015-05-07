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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.EventDispatch;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class NativeVirtualMachineMirror implements VirtualMachineMirror {

    public static NativeVirtualMachineMirror INSTANCE = new NativeVirtualMachineMirror();
    
    private static final Method nativeMethod;
    static {
        try {
            nativeMethod = ClassLoader.class.getDeclaredMethod("findBootstrapClass", String.class);
            nativeMethod.setAccessible(true);
        } catch (SecurityException e) {
            throw new InternalError();
        } catch (NoSuchMethodException e) {
            throw new InternalError();
        }
    }
    
    public ClassMirror findBootstrapClassMirror(String name) {
        try {
            // TODO-RS: Do we need to call the native findBootstrapClass() method directly?
            // Is this shortcut harmful?
            Class<?> klass = Class.forName(name, false, null);
            if (klass.getClassLoader() == null) {
                return new NativeClassMirror(klass);
            } else {
                return null;
            }
        } catch (ClassNotFoundException e) {
            return null;
        }
//        ClassLoader appClassLoader = ClassLoader.getSystemClassLoader();
//        try {
//            return (ClassMirror)NativeInstanceMirror.makeMirror((Class<?>)nativeMethod.invoke(appClassLoader, name));
//        } catch (IllegalAccessException e) {
//            throw new IllegalAccessError(e.getMessage());
//        } catch (InvocationTargetException e) {
//            throw new RuntimeException(e);
//        }
    }
    
    @Override
    public Enumeration<URL> findBootstrapResources(String path) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ClassMirror defineBootstrapClass(String name, ByteArrayMirror b, int off, int len) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public List<ClassMirror> findAllClasses() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public List<ClassMirror> findAllClasses(String name, boolean includeSubclasses) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public List<ThreadMirror> getThreads() {
        throw new UnsupportedOperationException();
    }
    
    public static Class<?> getNativePrimitiveClass(String name) {
        if (name.equals("boolean")) {
            return Boolean.TYPE;
        } else if (name.equals("byte")) {
            return Byte.TYPE;
        } else if (name.equals("char")) {
            return Character.TYPE;
        } else if (name.equals("short")) {
            return Short.TYPE;
        } else if (name.equals("int")) {
            return Integer.TYPE;
        } else if (name.equals("long")) {
            return Long.TYPE;
        } else if (name.equals("float")) {
            return Float.TYPE;
        } else if (name.equals("double")) {
            return Double.TYPE;
        } else if (name.equals("void")) {
            return Void.TYPE;
        } else {
            throw new IllegalArgumentException(name);
        }
    }
    
    @Override
    public ClassMirror getPrimitiveClass(String name) {
        return new NativeClassMirror(getNativePrimitiveClass(name));
    }
    
    @Override
    public ClassMirror getArrayClass(int dimensions, ClassMirror elementClass) {
        return new ArrayClassMirror(dimensions, elementClass);
    }

    @Override
    public MirrorEventRequestManager eventRequestManager() {
	throw new UnsupportedOperationException();
    }

    @Override
    public MirrorEventQueue eventQueue() {
	throw new UnsupportedOperationException();
    }

    @Override
    public void suspend() {
	throw new UnsupportedOperationException(); 
    }
    
    @Override
    public void resume() {
	throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean canBeModified() {
        // Not really, but I don't want to implement it. :)
        return false;
    }
    
    @Override
    public boolean canGetBytecodes() {
        return true;
    }
    
    public boolean hasClassInitialization() {
        return true;
    };
    
    @Override
    public InstanceMirror makeString(String s) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public InstanceMirror getInternedString(String s) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public EventDispatch dispatch() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void addCallback(MethodMirrorHandlerRequest request, Callback<MirrorEvent> callback) {
        throw new UnsupportedOperationException();
    }
    
    public static List<AnnotationMirror> makeAnnotations(List<Annotation> annotations) {
        List<AnnotationMirror> result = new ArrayList<AnnotationMirror>();
        for (Annotation a : annotations) {
            result.add(new NativeAnnotationMirror(a));
        }
        return result;
    }
}
