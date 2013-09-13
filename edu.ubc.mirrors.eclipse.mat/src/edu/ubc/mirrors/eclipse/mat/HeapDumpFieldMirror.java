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
package edu.ubc.mirrors.eclipse.mat;

import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.FieldDescriptor;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;

public class HeapDumpFieldMirror implements FieldMirror {

    private final HeapDumpClassMirror declaringClass;
    final FieldDescriptor fieldDescriptor;
    
    public HeapDumpFieldMirror(HeapDumpClassMirror declaringClass, FieldDescriptor fieldDescriptor) {
        this.declaringClass = declaringClass;
        this.fieldDescriptor = fieldDescriptor;
    }
    
    boolean isStatic() {
        return fieldDescriptor instanceof Field;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HeapDumpFieldMirror)) {
            return false;
        }
        
        HeapDumpFieldMirror other = (HeapDumpFieldMirror)obj;
        return declaringClass.equals(other.declaringClass) && fieldDescriptor.equals(other.fieldDescriptor);
    }
    
    @Override
    public int hashCode() {
        return 7 * declaringClass.hashCode() * fieldDescriptor.hashCode();
    }
    
    @Override
    public ClassMirror getDeclaringClass() {
        return declaringClass;
    }

    @Override
    public String getName() {
        return fieldDescriptor.getName();
    }
    
    @Override
    public ClassMirror getType() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String getTypeName() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int getModifiers() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + fieldDescriptor.getVerboseSignature() + " " + fieldDescriptor.getName();
    }
}
