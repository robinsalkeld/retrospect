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

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.wrapping.WrappingFieldMirror;

public class MutableFieldMirror extends WrappingFieldMirror {

    private final ClassHolograph klass;
    
    private FieldMirror bytecodeField;
    
    public MutableFieldMirror(ClassHolograph klass, FieldMirror immutableFieldMirror) {
        super(klass.getVM(), immutableFieldMirror);
        this.klass = klass;
    }
    
    @Override
    public ClassMirror getType() {
        if (klass.hasBytecode()) {
            return super.getType();
        } else {
            return getBytecodeField().getType();
        }
    }
    
    @Override
    public String getTypeName() {
        if (klass.hasBytecode()) {
            return super.getTypeName();
        } else {
            return getBytecodeField().getTypeName();
        }
    }
    
    @Override
    public int getModifiers() {
        // TODO-RS: For the benefit of TODVirtualMachineMirror
//        if (klass.hasBytecode()) {
        try {
            return super.getModifiers();
        } catch (UnsupportedOperationException e) {
//        } else {
            return getBytecodeField().getModifiers();
        }
    }
    
    private FieldMirror getBytecodeField() {
        if (bytecodeField == null) {
            bytecodeField = klass.getBytecodeMirror().getDeclaredField(getName());
        }
        return bytecodeField;
    }
}
