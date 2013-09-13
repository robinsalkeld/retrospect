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
import java.util.Map;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

public class HolographThreadGroupReference extends Holograph implements ThreadGroupReference {

    final ThreadGroupReference wrapped;
    
    /**
     * @return
     * @see com.sun.jdi.Mirror#toString()
     */
    public String toString() {
        return wrapped.toString();
    }

    /**
     * @return
     * @see com.sun.jdi.Value#type()
     */
    public Type type() {
        return wrapped.type();
    }

    /**
     * @return
     * @see com.sun.jdi.ThreadGroupReference#name()
     */
    public String name() {
        return wrapped.name();
    }

    /**
     * @return
     * @see com.sun.jdi.ThreadGroupReference#parent()
     */
    public ThreadGroupReference parent() {
        return vm.wrapThreadGroup(wrapped.parent());
    }

    /**
     * 
     * @see com.sun.jdi.ThreadGroupReference#resume()
     */
    public void resume() {
        wrapped.resume();
    }

    /**
     * 
     * @see com.sun.jdi.ThreadGroupReference#suspend()
     */
    public void suspend() {
        wrapped.suspend();
    }

    /**
     * @return
     * @see com.sun.jdi.ThreadGroupReference#threadGroups()
     */
    public List threadGroups() {
        return vm.wrapThreadGroups(wrapped.threadGroups());
    }

    /**
     * 
     * @see com.sun.jdi.ObjectReference#disableCollection()
     */
    public void disableCollection() {
        wrapped.disableCollection();
    }

    /**
     * @return
     * @see com.sun.jdi.ThreadGroupReference#threads()
     */
    public List threads() {
        return vm.wrapThreads(wrapped.threads());
    }

    /**
     * 
     * @see com.sun.jdi.ObjectReference#enableCollection()
     */
    public void enableCollection() {
        wrapped.enableCollection();
    }

    /**
     * @return
     * @throws IncompatibleThreadStateException
     * @see com.sun.jdi.ObjectReference#entryCount()
     */
    public int entryCount() throws IncompatibleThreadStateException {
        return wrapped.entryCount();
    }

    /**
     * @param arg1
     * @return
     * @see com.sun.jdi.ObjectReference#getValue(com.sun.jdi.Field)
     */
    public Value getValue(Field arg1) {
        return wrapped.getValue(arg1);
    }

    /**
     * @param arg1
     * @return
     * @see com.sun.jdi.ObjectReference#getValues(java.util.List)
     */
    public Map getValues(List arg1) {
        return wrapped.getValues(arg1);
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
     * @see com.sun.jdi.ObjectReference#invokeMethod(com.sun.jdi.ThreadReference, com.sun.jdi.Method, java.util.List, int)
     */
    public Value invokeMethod(ThreadReference arg1, Method arg2, List arg3,
            int arg4) throws InvalidTypeException, ClassNotLoadedException,
            IncompatibleThreadStateException, InvocationException {
        return wrapped.invokeMethod(arg1, arg2, arg3, arg4);
    }

    /**
     * @return
     * @see com.sun.jdi.ObjectReference#isCollected()
     */
    public boolean isCollected() {
        return wrapped.isCollected();
    }

    /**
     * @return
     * @throws IncompatibleThreadStateException
     * @see com.sun.jdi.ObjectReference#owningThread()
     */
    public ThreadReference owningThread()
            throws IncompatibleThreadStateException {
        return vm.wrapThread(wrapped.owningThread());
    }

    /**
     * @return
     * @see com.sun.jdi.ObjectReference#referenceType()
     */
    public ReferenceType referenceType() {
        return wrapped.referenceType();
    }

    /**
     * @param arg1
     * @param arg2
     * @throws InvalidTypeException
     * @throws ClassNotLoadedException
     * @see com.sun.jdi.ObjectReference#setValue(com.sun.jdi.Field, com.sun.jdi.Value)
     */
    public void setValue(Field arg1, Value arg2) throws InvalidTypeException,
            ClassNotLoadedException {
        wrapped.setValue(arg1, arg2);
    }

    /**
     * @return
     * @see com.sun.jdi.ObjectReference#uniqueID()
     */
    public long uniqueID() {
        return wrapped.uniqueID();
    }

    /**
     * @return
     * @throws IncompatibleThreadStateException
     * @see com.sun.jdi.ObjectReference#waitingThreads()
     */
    public List waitingThreads() throws IncompatibleThreadStateException {
        return vm.wrapThreads(wrapped.waitingThreads());
    }

    /**
     * @param arg1
     * @return
     * @see com.sun.jdi.ObjectReference#referringObjects(long)
     */
    public List referringObjects(long arg1) {
        return wrapped.referringObjects(arg1);
    }

    public HolographThreadGroupReference(
            JDIHolographVirtualMachine vm,
            ThreadGroupReference wrapped) { 
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

}
