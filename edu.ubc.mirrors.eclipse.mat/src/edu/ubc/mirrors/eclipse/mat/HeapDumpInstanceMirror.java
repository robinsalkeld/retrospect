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

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.ObjectReference;

import edu.ubc.mirrors.BoxingInstanceMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.ObjectMirror;

public class HeapDumpInstanceMirror extends BoxingInstanceMirror implements HeapDumpObjectMirror {

    protected final HeapDumpVirtualMachineMirror vm;
    protected final IInstance heapDumpObject;
    
    public HeapDumpInstanceMirror(HeapDumpVirtualMachineMirror vm, IInstance heapDumpObject) {
        this.vm = vm;
        this.heapDumpObject = heapDumpObject;
    }
    
    @Override
    public boolean equals(Object obj) {
        return HeapDumpVirtualMachineMirror.equalObjects(this, obj);
    }
    
    @Override
    public int hashCode() {
        return 11 * heapDumpObject.hashCode();
    }
    
    public IInstance getHeapDumpObject() {
        return heapDumpObject;
    }
    
    public HeapDumpClassMirror getClassMirror() {
        return (HeapDumpClassMirror)vm.makeMirror(heapDumpObject.getClazz());
    }
    
    @Override
    public int identityHashCode() {
        return vm.identityHashCode(heapDumpObject);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + heapDumpObject;
    }

    @Override
    public Object getBoxedValue(FieldMirror field) throws IllegalAccessException {
        int fieldOffset = getClassMirror().getFieldOffset(field);
        return heapDumpObject.getFields().get(fieldOffset).getValue();
    }

    @Override
    public ObjectMirror get(FieldMirror field) throws IllegalAccessException {
        Object value = getBoxedValue(field);
        ObjectReference ref = (ObjectReference)value;
        if (ref == null) {
            return null;
        }
        try {
            return vm.makeMirror(ref.getObject());
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void setBoxedValue(FieldMirror field, Object o) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }
}
