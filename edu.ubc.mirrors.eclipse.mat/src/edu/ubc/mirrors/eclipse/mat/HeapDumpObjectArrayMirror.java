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

import java.lang.ref.WeakReference;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;

import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

public class HeapDumpObjectArrayMirror implements ObjectArrayMirror, HeapDumpObjectMirror {

    private final HeapDumpVirtualMachineMirror vm;
    private final IObjectArray array;
    
    private WeakReference<long[]> referenceArray;
    
    public HeapDumpObjectArrayMirror(HeapDumpVirtualMachineMirror vm, IObjectArray array) {
        this.vm = vm;
        this.array = array;
    }
    
    @Override
    public boolean equals(Object obj) {
        return HeapDumpVirtualMachineMirror.equalObjects(this, obj);
    }
    
    @Override
    public int hashCode() {
        return 11 * array.hashCode();
    }
    
    public HeapDumpClassMirror getClassMirror() {
        return (HeapDumpClassMirror)vm.makeMirror(array.getClazz());
    }

    @Override
    public int identityHashCode() {
        return vm.identityHashCode(array);
    }
    
    @Override
    public IObject getHeapDumpObject() {
        return array;
    }
    
    public int length() {
        return array.getLength();
    }

    public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
        long[] refArray = referenceArray != null ? referenceArray.get() : null;
        if (refArray == null) {
            refArray = ((IObjectArray)array).getReferenceArray();
            referenceArray = new WeakReference<long[]>(refArray);
        }
        
        long address = refArray[index];
        if (address == 0) {
            return null;
        }
        try {
            IObject obj = array.getSnapshot().getObject(array.getSnapshot().mapAddressToId(address));
            return vm.makeMirror(obj);
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
    }

    public void set(int index, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
    @Override
    public String toString() {
        return "HeapDumpObjectArrayMirror: " + array;
    }
    
    @Override
    public void allowCollection(boolean flag) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isCollected() {
        throw new UnsupportedOperationException();
    }
}
