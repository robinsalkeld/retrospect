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

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

/**
 * Utility methods for the internal holograph implementation.
 * In many cases these are simple thunk functions that translate checked exceptions
 * into unchecked errors in the same way a JVM will for reflective calls it makes
 * (e.g. Class.forName() to resolve references in bytecode).
 * @author robinsalkeld
 */
public class HolographInternalUtils {

    public static ObjectMirror getField(InstanceMirror o, String name) {
        try {
            return o.get(Reflection.findField(o.getClassMirror(), name));
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }

    public static int getIntField(InstanceMirror o, String name) {
        try {
            return o.getInt(Reflection.findField(o.getClassMirror(), name));
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }

    public static void setField(InstanceMirror o, String name, ObjectMirror value) {
        try {
            o.set(Reflection.findField(o.getClassMirror(), name), value);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }

    public static void setField(InstanceMirror o, String name, int value) {
        try {
            o.setInt(Reflection.findField(o.getClassMirror(), name), value);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }

    public static ClassMirror loadClassMirrorInternal(ClassMirror context, String name) {
        return loadClassMirrorInternal(context.getVM(), context.getLoader(), name);
    }

    public static ClassMirror loadClassMirrorInternal(VirtualMachineMirror vm, ClassMirrorLoader loader, String name) {
        try {
            // Need to use ThreadHolograph.currentThreadMirrorNoError() in case we are trying to load a bootstrap
            // class and don't have an active thread. This is safe since downstream methods will check for a valid
            // thread if they need one.
            return Reflection.classMirrorForName(vm, ThreadHolograph.currentThreadMirrorNoError(), name, false, loader);
        } catch (ClassNotFoundException e) {
            NoClassDefFoundError error = new NoClassDefFoundError(e.getMessage());
            error.initCause(e);
            throw error;
        } catch (MirrorInvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static ClassMirror classMirrorForType(VirtualMachineMirror vm, ThreadMirror thread, Type type, boolean resolve, ClassMirrorLoader loader) {
        try {
            return Reflection.classMirrorForType(vm, thread, type, false, loader);
        } catch (ClassNotFoundException e) {
            NoClassDefFoundError error = new NoClassDefFoundError(e.getMessage());
            error.initCause(e);
            throw error;
        } catch (MirrorInvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static MethodMirror getMethod(ClassMirror klass, String name, String... parameterTypeNames) {
        try {
            return klass.getMethod(name, parameterTypeNames);
        } catch (NoSuchMethodException e) {
            NoSuchMethodError error = new NoSuchMethodError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }

    public static ConstructorMirror getConstructor(ClassMirror klass, String... paramTypeNames) {
        try {
            return klass.getConstructor(paramTypeNames);
        } catch (NoSuchMethodException e) {
            NoSuchMethodError error = new NoSuchMethodError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }

    public static Object mirrorInvoke(ThreadMirror thread, MethodMirror method, ObjectMirror obj, Object... args) {
        try {
            return method.invoke(thread, obj, args);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IllegalAccessException e) {
            IllegalAccessError error = new IllegalAccessError(e.getMessage());
            error.initCause(e);
            throw error;
        } catch (MirrorInvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static InstanceMirror newInstance(ConstructorMirror constructor, ThreadMirror thread, Object... args) {
        try {
            return constructor.newInstance(thread, args);
        } catch (IllegalAccessException e) {
            IllegalAccessError error = new IllegalAccessError(e.getMessage());
            error.initCause(e);
            throw error;
        } catch (MirrorInvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static InstanceMirror stackTraceElementForFrameMirror(VirtualMachineMirror vm, FrameMirror frame) {
        try {
            ClassMirror stackTraceElementClass = vm.findBootstrapClassMirror(StackTraceElement.class.getName());
            InstanceMirror element = stackTraceElementClass.newRawInstance();
            element.set(stackTraceElementClass.getDeclaredField("declaringClass"), vm.makeString(frame.declaringClass().getClassName()));
            element.set(stackTraceElementClass.getDeclaredField("methodName"), vm.makeString(frame.methodName()));
            element.set(stackTraceElementClass.getDeclaredField("fileName"), vm.makeString(frame.fileName()));
            element.setInt(stackTraceElementClass.getDeclaredField("lineNumber"), frame.lineNumber());
            return element;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
