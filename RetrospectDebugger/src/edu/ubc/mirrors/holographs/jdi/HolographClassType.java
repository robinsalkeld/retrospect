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
package edu.ubc.mirrors.holographs.jdi;

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
import com.sun.jdi.VirtualMachine;

public class HolographClassType extends HolographReferenceType implements ClassType {

    private final ClassType wrapped;
    
    public HolographClassType(JDIHolographVirtualMachine vm, ClassType wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    /**
     * @return
     * @see com.sun.jdi.ClassType#allInterfaces()
     */
    public List<InterfaceType> allInterfaces() {
        return wrapped.allInterfaces();
    }

    /**
     * @param arg1
     * @param arg2
     * @return
     * @see com.sun.jdi.ClassType#concreteMethodByName(java.lang.String, java.lang.String)
     */
    public Method concreteMethodByName(String arg1, String arg2) {
        return wrapped.concreteMethodByName(arg1, arg2);
    }

    /**
     * @return
     * @see com.sun.jdi.ClassType#interfaces()
     */
    public List<InterfaceType> interfaces() {
        return wrapped.interfaces();
    }

    /**
     * @param arg1
     * @param arg2
     * @param arg3
     * @param arg4
     * @return
     * @throws InvalidTypeException
     * @throws ClassNotLoadedException
     * @throws IncompatibleThreadStateException
     * @throws InvocationException
     * @see com.sun.jdi.ClassType#invokeMethod(com.sun.jdi.ThreadReference, com.sun.jdi.Method, java.util.List, int)
     */
    public Value invokeMethod(ThreadReference arg1, Method arg2, List arg3,
            int arg4) throws InvalidTypeException, ClassNotLoadedException,
            IncompatibleThreadStateException, InvocationException {
        ThreadReference unwrappedThread = vm.unwrapThread(arg1);
        List unwrappedArgs = vm.unwrapObjectReferences(arg3);
        return wrapped.invokeMethod(unwrappedThread, arg2, unwrappedArgs, arg4);
    }

    /**
     * @return
     * @see com.sun.jdi.ClassType#isEnum()
     */
    public boolean isEnum() {
        return wrapped.isEnum();
    }

    /**
     * @param arg1
     * @param arg2
     * @param arg3
     * @param arg4
     * @return
     * @throws InvalidTypeException
     * @throws ClassNotLoadedException
     * @throws IncompatibleThreadStateException
     * @throws InvocationException
     * @see com.sun.jdi.ClassType#newInstance(com.sun.jdi.ThreadReference, com.sun.jdi.Method, java.util.List, int)
     */
    public ObjectReference newInstance(ThreadReference arg1, Method arg2, List arg3, int arg4) throws InvalidTypeException,
            ClassNotLoadedException, IncompatibleThreadStateException,
            InvocationException {
        return wrapped.newInstance(arg1, arg2, arg3, arg4);
    }

    /**
     * @param arg1
     * @param arg2
     * @throws InvalidTypeException
     * @throws ClassNotLoadedException
     * @see com.sun.jdi.ClassType#setValue(com.sun.jdi.Field, com.sun.jdi.Value)
     */
    public void setValue(Field arg1, Value arg2) throws InvalidTypeException,
            ClassNotLoadedException {
        wrapped.setValue(arg1, arg2);
    }

    /**
     * @return
     * @see com.sun.jdi.ClassType#subclasses()
     */
    public List subclasses() {
        return wrapped.subclasses();
    }

    /**
     * @return
     * @see com.sun.jdi.ClassType#superclass()
     */
    public ClassType superclass() {
        return wrapped.superclass();
    }

}
