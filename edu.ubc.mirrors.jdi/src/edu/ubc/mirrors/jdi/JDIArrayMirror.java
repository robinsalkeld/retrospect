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
package edu.ubc.mirrors.jdi;

import java.util.List;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

import edu.ubc.mirrors.BoxingArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

public class JDIArrayMirror extends BoxingArrayMirror implements JDIObjectMirror, ObjectArrayMirror {

    private final JDIVirtualMachineMirror vm;
    private final ArrayReference array;
    
    public JDIArrayMirror(JDIVirtualMachineMirror vm, ArrayReference array) {
        this.vm = vm;
        this.array = array;
        this.array.disableCollection();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        
        return ((JDIArrayMirror)obj).array.equals(array);
    }
    
    @Override
    public int hashCode() {
        return 11 * array.hashCode();
    }
    
    @Override
    public ObjectReference getObjectReference() {
        return array;
    }
    
    @Override
    public int length() {
        return array.length();
    }

    @Override
    public ClassMirror getClassMirror() {
        return vm.makeClassMirror(array.referenceType().classObject());
    }

    @Override
    public int identityHashCode() {
        return vm.identityHashCode(array);
    }
    
    @Override
    public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
        return vm.makeMirror((ObjectReference)array.getValue(index));
    }

    @Override
    protected Object getBoxedValue(int index) throws ArrayIndexOutOfBoundsException {
        return vm.wrapValue(array.getValue(index));
    }

    @Override
    public byte[] getBytes(int index, int length) throws ArrayIndexOutOfBoundsException {
        // Special case to avoid incorrect range check in JDI code
        if (length == 0) {
            return new byte[0];
        }
        
        List<Value> values = array.getValues(index, length);
        byte[] result = new byte[values.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = ((ByteValue)values.get(i)).byteValue();
        }
        return result;
    }
    
    @Override
    public char[] getChars(int index, int length) throws ArrayIndexOutOfBoundsException {
        // Special case to avoid incorrect range check in JDI code
        if (length == 0) {
            return new char[0];
        }
        
        List<Value> values = array.getValues(index, length);
        char[] result = new char[values.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = ((CharValue)values.get(i)).charValue();
        }
        return result;
    }
    
    @Override
    public int[] getInts(int index, int length) throws ArrayIndexOutOfBoundsException {
        // Special case to avoid incorrect range check in JDI code
        if (length == 0) {
            return new int[0];
        }
        
        List<Value> values = array.getValues(index, length);
        int[] result = new int[values.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = ((IntegerValue)values.get(i)).intValue();
        }
        return result;
    }
    
    @Override
    public void set(int index, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        try {
            array.setValue(index, ((AbstractJDIObjectMirror)o).mirror);
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        } catch (ClassNotLoadedException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected void setBoxedValue(int index, Object o) throws ArrayIndexOutOfBoundsException {
        try {
            array.setValue(index, vm.toValue(o));
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        } catch (ClassNotLoadedException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
    @Override
    public void allowCollection(boolean flag) {
        if (flag) {
            array.enableCollection();
        } else {
            array.disableCollection();
        }
    }
    
    @Override
    public boolean isCollected() {
        return array.isCollected();
    }
}
