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

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.asjdi.MirrorsMirrorWithModifiers;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class ConstructorMirrorMethod extends MirrorsMirrorWithModifiers implements Method {

    protected final ConstructorMirror wrapped;
    
    public ConstructorMirrorMethod(MirrorsVirtualMachine vm, ConstructorMirror wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public ReferenceType declaringType() {
        return (ReferenceType)vm.typeForClassMirror(wrapped.getDeclaringClass());
    }

    @Override
    public String genericSignature() {
        return null;
    }

    @Override
    public String signature() {
        return wrapped.getSignature();
    }

    @Override
    public int modifiers() {
        return wrapped.getModifiers();
    }

    @Override
    public int compareTo(Method o) {
        return signature().compareTo(o.signature());
    }

    @Override
    public List<Location> allLineLocations() throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public List<Location> allLineLocations(String arg0, String arg1) throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public List<String> argumentTypeNames() {
        return wrapped.getParameterTypeNames();
    }

    @Override
    public List<Type> argumentTypes() throws ClassNotLoadedException {
        List<Type> result = new ArrayList<Type>();
        for (ClassMirror argType : wrapped.getParameterTypes()) {
            result.add(vm.typeForClassMirror(argType));
        }
        return result;
    }

    @Override
    public List<LocalVariable> arguments() throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public byte[] bytecodes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isConstructor() {
        return true;
    }

    @Override
    public boolean isStaticInitializer() {
        return false;
    }

    @Override
    public Location location() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location locationOfCodeIndex(long arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Location> locationsOfLine(int arg0) throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public List<Location> locationsOfLine(String arg0, String arg1, int arg2)
            throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public List<LocalVariable> variables() throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public List<LocalVariable> variablesByName(String arg0) throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public boolean isObsolete() {
        return false;
    }

    @Override
    public String name() {
        return "<init>";
    }

    @Override
    public Type returnType() throws ClassNotLoadedException {
        return declaringType();
    }

    @Override
    public String returnTypeName() {
        return declaringType().name();
    }
}
