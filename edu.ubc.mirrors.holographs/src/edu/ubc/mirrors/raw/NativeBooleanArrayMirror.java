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

import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class NativeBooleanArrayMirror extends NativeObjectMirror implements BooleanArrayMirror {

    private final boolean[] array;
    
    public NativeBooleanArrayMirror(boolean[] array) {
        super(array);
        this.array = array;
    }
    
    public int length() {
        return array.length;
    }

    public boolean getBoolean(int index) throws ArrayIndexOutOfBoundsException {
        return array[index];
    }

    public void setBoolean(int index, boolean b) throws ArrayIndexOutOfBoundsException {
        array[index] = b;
    }

    @Override
    public byte getByte(int index) throws ArrayIndexOutOfBoundsException {
        return getBoolean(index) ? (byte)1 : 0;
    }
    
    public void setByte(int index, byte b) throws ArrayIndexOutOfBoundsException {
        setBoolean(index, b != 0);
    };
}
