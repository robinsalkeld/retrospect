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
package edu.ubc.mirrors.raw;

import java.util.Arrays;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class NativeByteArrayMirror extends NativeObjectMirror implements ByteArrayMirror {

    private final byte[] array;
    
    public NativeByteArrayMirror(byte[] array) {
        super(array);
        this.array = array;
    }
    
    public int length() {
        return array.length;
    }

    public byte getByte(int index) throws ArrayIndexOutOfBoundsException {
        return array[index];
    }

    @Override
    public byte[] getBytes(int index, int length) throws ArrayIndexOutOfBoundsException {
        return Arrays.copyOfRange(array, index, index + length);
    }
    
    public void setByte(int index, byte b) throws ArrayIndexOutOfBoundsException {
        array[index] = b;
    }

    @Override
    public void setBytes(int index, byte[] b) throws ArrayIndexOutOfBoundsException {
        System.arraycopy(b, 0, array, index, b.length);
    }
}
