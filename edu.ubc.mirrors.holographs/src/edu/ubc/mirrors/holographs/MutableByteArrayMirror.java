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
    public void setByte(int index, byte b) throws ArrayIndexOutOfBoundsException {
        if (values == null) {
            this.values = new byte[immutableMirror.length()];
            for (int i = 0; i < values.length; i++) {
                values[i] = immutableMirror.getByte(i);
            }
        }
        values[index] = b;
    }
}
