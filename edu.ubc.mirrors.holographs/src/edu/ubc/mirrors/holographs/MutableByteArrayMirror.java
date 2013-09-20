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

import java.util.Arrays;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.wrapping.WrappingByteArrayMirror;

public class MutableByteArrayMirror extends WrappingByteArrayMirror {

    private final ByteArrayMirror immutableMirror;
    private byte[] values;
    
    public MutableByteArrayMirror(VirtualMachineHolograph vm, ByteArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.immutableMirror = immutableMirror;
    }
    
    @Override
    public byte getByte(int index) throws ArrayIndexOutOfBoundsException {
        return values != null ? values[index] : immutableMirror.getByte(index);
    }

    @Override
    public byte[] getBytes(int index, int length) throws ArrayIndexOutOfBoundsException {
        if (values != null) {
            if (index == 0 && length == values.length) {
                return values;
            } else {
                return Arrays.copyOfRange(values, index, index + length);
            }
        } else {
            return super.getBytes(index, length);
        }
    }
    
    private void copyOnWrite() {
        if (values == null) {
            this.values = immutableMirror.getBytes(0, length());
        }
    }
    
    @Override
    public void setByte(int index, byte b) throws ArrayIndexOutOfBoundsException {
        copyOnWrite();
        values[index] = b;
    }

    @Override
    public void setBytes(int index, byte[] b) throws ArrayIndexOutOfBoundsException {
        copyOnWrite();
        System.arraycopy(b, 0, values, index, b.length);
    }
}
