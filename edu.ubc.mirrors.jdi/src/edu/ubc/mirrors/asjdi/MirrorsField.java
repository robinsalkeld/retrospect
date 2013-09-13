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

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.asjdi.MirrorsMirrorWithModifiers;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsField extends MirrorsMirrorWithModifiers implements Field {

    protected final FieldMirror wrapped;
    
    public MirrorsField(MirrorsVirtualMachine vm, FieldMirror wrapped) {
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
    public String name() {
        return wrapped.getName();
    }

    @Override
    public String signature() {
        return Reflection.typeForClassMirror(wrapped.getType()).getDescriptor();
    }

    @Override
    public int modifiers() {
        return wrapped.getModifiers();
    }

    @Override
    public int compareTo(Field o) {
        return name().compareTo(o.name());
    }

    @Override
    public boolean isEnumConstant() {
        return (modifiers() & 0x00004000) != 0;
    }

    @Override
    public Type type() throws ClassNotLoadedException {
        return vm.typeForClassMirror(wrapped.getType());
    }

    @Override
    public String typeName() {
        return wrapped.getTypeName();
    }

}
