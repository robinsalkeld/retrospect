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
package edu.ubc.mirrors.asjdi;

import java.util.List;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ThreadMirror;

public class MirrorsClassType extends MirrorsReferenceType implements ClassType {

    public MirrorsClassType(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
        assert !wrapped.isInterface() && !wrapped.isArray();
    }

    @Override
    public List<InterfaceType> allInterfaces() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Method concreteMethodByName(String name, String signature) {
        for (Method method : methods()) {
            if (method.name().equals(name) && method.signature().equals(signature)) {
                return method.isAbstract() ? null : method;
            }
        }
        
        if (wrapped.getSuperClassMirror() != null) {
            return superclass().concreteMethodByName(name, signature);
        }
        
        return null;
    }
    
    @Override
    public List<Method> allMethods() {
        List<Method> result = super.methods();
        
        if (wrapped.getSuperClassMirror() != null) {
            result.addAll(superclass().methods());
        }
        
        return result;
    }
    
    @Override
    public List<Method> methodsByName(String name) {
        List<Method> result = super.methodsByName(name);
        
        if (wrapped.getSuperClassMirror() != null) {
            result.addAll(superclass().methodsByName(name));
        }
        
        return result;
    }

    @Override
    public Value invokeMethod(ThreadReference thread, Method method,
            List<? extends Value> args, int options) throws InvalidTypeException,
            ClassNotLoadedException, IncompatibleThreadStateException,
            InvocationException {
        
        return ((MethodMirrorMethod)method).invoke(thread, null, args, options);
    }

    @Override
    public ObjectReference newInstance(ThreadReference thread, Method method,
            List<? extends Value> args, int options) throws InvalidTypeException,
            ClassNotLoadedException, IncompatibleThreadStateException,
            InvocationException {
        
        if (options != ObjectReference.INVOKE_SINGLE_THREADED) {
            throw new IllegalArgumentException("Unsupported options: " + options);
        }
        
        ThreadMirror threadMirror = ((MirrorsThreadReference)thread).wrapped;
        Object[] mirrorArgs = vm.objectsForValues(args);
        
        Object result;
        try {
            result = ((ConstructorMirrorMethod)method).wrapped.newInstance(threadMirror, mirrorArgs);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (MirrorInvocationTargetException e) {
            throw new InvocationException(vm.wrapMirror(e.getTargetException()));
        }
        return vm.wrapMirror((InstanceMirror)result);
    }

    @Override
    public void setValue(Field field, Value value) throws InvalidTypeException,
            ClassNotLoadedException {
        vm.setValue(wrapped.getStaticFieldValues(), field, value);
    }

    @Override
    public List<ClassType> subclasses() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassType superclass() {
        return (ClassType)vm.typeForClassMirror(wrapped.getSuperClassMirror());
    }

}
