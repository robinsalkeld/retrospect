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

import edu.ubc.mirrors.CharArrayMirror;

public class WrappingCharArrayMirror extends WrappingMirror implements CharArrayMirror {

    private final CharArrayMirror wrappedArray;
    
    public WrappingCharArrayMirror(WrappingVirtualMachine vm, CharArrayMirror wrappedArray) {
        super(vm, wrappedArray);
        this.wrappedArray = wrappedArray;
    }

    @Override
    public int length() {
        return wrappedArray.length();
    }

    @Override
    public char getChar(int index) throws ArrayIndexOutOfBoundsException {
        return wrappedArray.getChar(index);
    }
    
    @Override
    public char[] getChars(int index, int length) throws ArrayIndexOutOfBoundsException {
        return wrappedArray.getChars(index, length);
    }

    @Override
    public void setChar(int index, char c) throws ArrayIndexOutOfBoundsException {
        wrappedArray.setChar(index, c);
    }
    
    @Override
    public void setChars(int index, char[] c) throws ArrayIndexOutOfBoundsException {
        wrappedArray.setChars(index, c);
    }
}
