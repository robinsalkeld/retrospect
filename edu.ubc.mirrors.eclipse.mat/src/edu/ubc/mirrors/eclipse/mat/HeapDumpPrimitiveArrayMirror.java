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

import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;

import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.BoxingArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.DoubleArrayMirror;
import edu.ubc.mirrors.FloatArrayMirror;
import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.LongArrayMirror;
import edu.ubc.mirrors.ShortArrayMirror;

public class HeapDumpPrimitiveArrayMirror extends BoxingArrayMirror implements HeapDumpObjectMirror {
    
    private final HeapDumpVirtualMachineMirror vm;
    private final IPrimitiveArray array;
    
    private WeakReference<Object> valueArray;
    
    public HeapDumpPrimitiveArrayMirror(HeapDumpVirtualMachineMirror vm, IPrimitiveArray array) {
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
        return vm.makeClassMirror(array.getClazz());
    }
    public int length() {
        return array.getLength();
    }
    
    private Object getValueArray() {
        Object valArray = valueArray != null ? valueArray.get() : null;
        if (valArray == null) {
            valArray = ((IPrimitiveArray)array).getValueArray();
            valueArray = new WeakReference<Object>(valArray);
        }
        return valArray;
    }
    
    @Override
    public boolean getBoolean(int index) throws ArrayIndexOutOfBoundsException {
        return ((boolean[])getValueArray())[index];
    }
    
    @Override
    public byte getByte(int index) throws ArrayIndexOutOfBoundsException {
        return ((byte[])getValueArray())[index];
    }
    
    @Override
    public char getChar(int index) throws ArrayIndexOutOfBoundsException {
        return ((char[])getValueArray())[index];
    }
    
    @Override
    public double getDouble(int index) throws ArrayIndexOutOfBoundsException {
        return ((double[])getValueArray())[index];
    }
    
    @Override
    public float getFloat(int index) throws ArrayIndexOutOfBoundsException {
        return ((long[])getValueArray())[index];
    }
    
    @Override
    public int getInt(int index) throws ArrayIndexOutOfBoundsException {
        return ((int[])getValueArray())[index];
    }
    
    @Override
    public long getLong(int index) throws ArrayIndexOutOfBoundsException {
        return ((long[])getValueArray())[index];
    }
    
    @Override
    public short getShort(int index) throws ArrayIndexOutOfBoundsException {
        return ((short[])getValueArray())[index];
    }
    
    public Object getBoxedValue(int index) {
        // Not actually used
        throw new UnsupportedOperationException();
    }
    public void setBoxedValue(int index, Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IObject getHeapDumpObject() {
        return array;
    }
    
    @Override
    public int identityHashCode() {
        return vm.identityHashCode(array);
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
