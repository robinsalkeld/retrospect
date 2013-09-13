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

import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ReferenceType;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.asjdi.MirrorsArrayType;
import edu.ubc.mirrors.asjdi.MirrorsClassType;
import edu.ubc.mirrors.asjdi.MirrorsInterfaceType;
import edu.ubc.mirrors.asjdi.MirrorsObjectReference;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsClassObjectReference extends MirrorsObjectReference implements ClassObjectReference {

    private final ReferenceType reflectedType;
    
    public MirrorsClassObjectReference(MirrorsVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
        if (wrapped.isArray()) {
            reflectedType = new MirrorsArrayType(vm, wrapped);
        } else if (wrapped.isInterface()) {
            reflectedType = new MirrorsInterfaceType(vm, wrapped);
        } else {
            reflectedType = new MirrorsClassType(vm, wrapped);
        }
    }

    @Override
    public ReferenceType reflectedType() {
        return reflectedType;
    }
}
