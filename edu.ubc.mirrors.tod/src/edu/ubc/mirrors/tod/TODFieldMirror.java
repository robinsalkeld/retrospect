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
package edu.ubc.mirrors.tod;

import tod.core.database.structure.IFieldInfo;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;

public class TODFieldMirror implements FieldMirror {

    private final TODVirtualMachineMirror vm;
    protected final IFieldInfo field;

    public TODFieldMirror(TODVirtualMachineMirror vm, IFieldInfo field) {
        super();
        this.vm = vm;
        this.field = field;
    }

    @Override
    public ClassMirror getDeclaringClass() {
        return vm.makeClassMirror(field.getDeclaringType());
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public String getTypeName() {
        return field.getType().getName();
    }
    
    @Override
    public ClassMirror getType() {
        return vm.makeClassMirror(field.getType());
    }

    @Override
    public int getModifiers() {
        throw new UnsupportedOperationException();
    }
    
}
