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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;
import com.sun.jdi.VirtualMachine;

public class HolographMethod extends Holograph implements Method {

    final Method wrapped;

    public HolographMethod(JDIHolographVirtualMachine vm, Method wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    /**
     * @return
     * @see com.sun.jdi.Mirror#toString()
     */
    public String toString() {
        return wrapped.toString();
    }

    /**
     * @return
     * @see com.sun.jdi.Locatable#location()
     */
    public Location location() {
        return new HolographLocation(vm, location());
    }

    /**
     * @return
     * @see com.sun.jdi.Accessible#isPackagePrivate()
     */
    public boolean isPackagePrivate() {
        return wrapped.isPackagePrivate();
    }

    /**
     * @return
     * @see com.sun.jdi.TypeComponent#declaringType()
     */
    public ReferenceType declaringType() {
        return vm.wrapReferenceType(wrapped.declaringType());
    }

    /**
     * @return
     * @see com.sun.jdi.Accessible#isPrivate()
     */
    public boolean isPrivate() {
        return wrapped.isPrivate();
    }

    /**
     * @return
     * @see com.sun.jdi.Accessible#isProtected()
     */
    public boolean isProtected() {
        return wrapped.isProtected();
    }

    /**
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.Method#allLineLocations()
     */
    public List allLineLocations() throws AbsentInformationException {
        return vm.wrapLocations(wrapped.allLineLocations());
    }

    /**
     * @return
     * @see com.sun.jdi.TypeComponent#genericSignature()
     */
    public String genericSignature() {
        return wrapped.genericSignature();
    }

    /**
     * @return
     * @see com.sun.jdi.Accessible#isPublic()
     */
    public boolean isPublic() {
        return wrapped.isPublic();
    }

    /**
     * @return
     * @see com.sun.jdi.TypeComponent#isFinal()
     */
    public boolean isFinal() {
        return wrapped.isFinal();
    }

    /**
     * @return
     * @see com.sun.jdi.Accessible#modifiers()
     */
    public int modifiers() {
        return wrapped.modifiers();
    }

    /**
     * @return
     * @see com.sun.jdi.TypeComponent#isStatic()
     */
    public boolean isStatic() {
        return wrapped.isStatic();
    }

    /**
     * @param arg1
     * @param arg2
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.Method#allLineLocations(java.lang.String, java.lang.String)
     */
    public List allLineLocations(String arg1, String arg2) throws AbsentInformationException {
        return vm.wrapLocations(wrapped.allLineLocations(arg1, arg2));
    }

    /**
     * @return
     * @see com.sun.jdi.TypeComponent#isSynthetic()
     */
    public boolean isSynthetic() {
        return wrapped.isSynthetic();
    }

    /**
     * @return
     * @see com.sun.jdi.TypeComponent#name()
     */
    public String name() {
        return wrapped.name();
    }

    /**
     * @return
     * @see com.sun.jdi.TypeComponent#signature()
     */
    public String signature() {
        return wrapped.signature();
    }

    /**
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.Method#arguments()
     */
    public List arguments() throws AbsentInformationException {
        return wrapped.arguments();
    }

    /**
     * @return
     * @see com.sun.jdi.Method#argumentTypeNames()
     */
    public List argumentTypeNames() {
        return wrapped.argumentTypeNames();
    }

    /**
     * @return
     * @throws ClassNotLoadedException
     * @see com.sun.jdi.Method#argumentTypes()
     */
    public List argumentTypes() throws ClassNotLoadedException {
        return wrapped.argumentTypes();
    }

    /**
     * @return
     * @see com.sun.jdi.Method#bytecodes()
     */
    public byte[] bytecodes() {
        return wrapped.bytecodes();
    }

    /**
     * @return
     * @see com.sun.jdi.Method#isAbstract()
     */
    public boolean isAbstract() {
        return wrapped.isAbstract();
    }

    /**
     * @return
     * @see com.sun.jdi.Method#isBridge()
     */
    public boolean isBridge() {
        return wrapped.isBridge();
    }

    /**
     * @return
     * @see com.sun.jdi.Method#isConstructor()
     */
    public boolean isConstructor() {
        return wrapped.isConstructor();
    }

    /**
     * @return
     * @see com.sun.jdi.Method#isNative()
     */
    public boolean isNative() {
        return wrapped.isNative();
    }

    /**
     * @return
     * @see com.sun.jdi.Method#isObsolete()
     */
    public boolean isObsolete() {
        return wrapped.isObsolete();
    }

    /**
     * @return
     * @see com.sun.jdi.Method#isStaticInitializer()
     */
    public boolean isStaticInitializer() {
        return wrapped.isStaticInitializer();
    }

    /**
     * @return
     * @see com.sun.jdi.Method#isSynchronized()
     */
    public boolean isSynchronized() {
        return wrapped.isSynchronized();
    }

    /**
     * @return
     * @see com.sun.jdi.Method#isVarArgs()
     */
    public boolean isVarArgs() {
        return wrapped.isVarArgs();
    }

    /**
     * @param arg1
     * @return
     * @see com.sun.jdi.Method#locationOfCodeIndex(long)
     */
    public Location locationOfCodeIndex(long arg1) {
        return vm.wrapLocation(locationOfCodeIndex(arg1));
    }

    /**
     * @param arg1
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.Method#locationsOfLine(int)
     */
    public List locationsOfLine(int arg1) throws AbsentInformationException {
        return vm.wrapLocations(wrapped.locationsOfLine(arg1));
    }

    /**
     * @param arg1
     * @param arg2
     * @param arg3
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.Method#locationsOfLine(java.lang.String, java.lang.String, int)
     */
    public List locationsOfLine(String arg1, String arg2, int arg3)
            throws AbsentInformationException {
        return vm.wrapLocations(wrapped.locationsOfLine(arg1, arg2, arg3));
    }

    /**
     * @return
     * @throws ClassNotLoadedException
     * @see com.sun.jdi.Method#returnType()
     */
    public Type returnType() throws ClassNotLoadedException {
        return wrapped.returnType();
    }

    /**
     * @return
     * @see com.sun.jdi.Method#returnTypeName()
     */
    public String returnTypeName() {
        return wrapped.returnTypeName();
    }

    /**
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.Method#variables()
     */
    public List variables() throws AbsentInformationException {
        return wrapped.variables();
    }

    /**
     * @param arg1
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.Method#variablesByName(java.lang.String)
     */
    public List variablesByName(String arg1) throws AbsentInformationException {
        return wrapped.variablesByName(arg1);
    }

    /**
     * @param o
     * @return
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Method o) {
        return wrapped.compareTo(((HolographMethod)o).wrapped);
    }
}
