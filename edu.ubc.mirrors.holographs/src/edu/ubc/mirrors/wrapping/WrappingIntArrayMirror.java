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
package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.IntArrayMirror;

public class WrappingIntArrayMirror extends WrappingMirror implements IntArrayMirror {

    private final IntArrayMirror wrappedArray;
    
    public WrappingIntArrayMirror(WrappingVirtualMachine vm, IntArrayMirror wrappedArray) {
        super(vm, wrappedArray);
        this.wrappedArray = wrappedArray;
    }

    @Override
    public int length() {
        return wrappedArray.length();
    }

    @Override
    public int getInt(int index) throws ArrayIndexOutOfBoundsException {
        return wrappedArray.getInt(index);
    }

    public int[] getInts(int index, int length) {
        return wrappedArray.getInts(index, length);
    }
    
    @Override
    public void setInt(int index, int i) throws ArrayIndexOutOfBoundsException {
        wrappedArray.setInt(index, i);
    }
    
    @Override
    public void setInts(int index, int[] b) throws ArrayIndexOutOfBoundsException {
        wrappedArray.setInts(index, b);
    }
}
