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

import edu.ubc.mirrors.IntArrayMirror;

public class NativeIntArrayMirror extends NativeObjectMirror implements IntArrayMirror {

    private final int[] array;
    
    public NativeIntArrayMirror(int[] array) {
        super(array);
        this.array = array;
    }
    
    public int length() {
        return array.length;
    }

    public int getInt(int index) throws ArrayIndexOutOfBoundsException {
        return array[index];
    }

    @Override
    public int[] getInts(int index, int length) throws ArrayIndexOutOfBoundsException {
        return Arrays.copyOfRange(array, index, index + length);
    }
    
    public void setInt(int index, int i) throws ArrayIndexOutOfBoundsException {
        array[index] = i;
    }
    
    @Override
    public void setInts(int index, int[] b) throws ArrayIndexOutOfBoundsException {
        System.arraycopy(b, 0, array, index, b.length);
    }
}
