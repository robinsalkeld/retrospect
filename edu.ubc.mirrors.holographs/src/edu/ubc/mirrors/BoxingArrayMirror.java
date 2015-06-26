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
package edu.ubc.mirrors;


public abstract class BoxingArrayMirror implements
                                          BooleanArrayMirror, ByteArrayMirror, CharArrayMirror, ShortArrayMirror,
                                          IntArrayMirror, LongArrayMirror, FloatArrayMirror, DoubleArrayMirror {

    protected abstract Object getBoxedValue(int index) throws ArrayIndexOutOfBoundsException;
    
    protected abstract void setBoxedValue(int index, Object o) throws ArrayIndexOutOfBoundsException;
    
    public double getDouble(int index) throws ArrayIndexOutOfBoundsException {
        return ((Double)getBoxedValue(index)).doubleValue();
    }

    public void setDouble(int index, double d) throws ArrayIndexOutOfBoundsException {
        setBoxedValue(index, Double.valueOf(d));
    }

    public float getFloat(int index) throws ArrayIndexOutOfBoundsException {
        return ((Float)getBoxedValue(index)).floatValue();
    }

    public void setFloat(int index, float f) throws ArrayIndexOutOfBoundsException {
        setBoxedValue(index, Float.valueOf(f));
    }

    public long getLong(int index) throws ArrayIndexOutOfBoundsException {
        return ((Long)getBoxedValue(index)).longValue();
    }

    public void setLong(int index, long l) throws ArrayIndexOutOfBoundsException {
        setBoxedValue(index, Long.valueOf(l));
    }

    public int getInt(int index) throws ArrayIndexOutOfBoundsException {
        return ((Integer)getBoxedValue(index)).intValue();
    }

    public void setInt(int index, int i) throws ArrayIndexOutOfBoundsException {
        setBoxedValue(index, Integer.valueOf(i));
    }

    public short getShort(int index) throws ArrayIndexOutOfBoundsException {
        return ((Short)getBoxedValue(index)).shortValue();
    }

    public void setShort(int index, short b) throws ArrayIndexOutOfBoundsException {
        setBoxedValue(index, Short.valueOf(b));
    }

    public char getChar(int index) throws ArrayIndexOutOfBoundsException {
        return ((Character)getBoxedValue(index)).charValue();
    }

    @Override
    public char[] getChars(int index, int length) throws ArrayIndexOutOfBoundsException {
        char[] result = new char[length];
        for (int i = 0; i < length; i++) {
            result[i] = getChar(index + i);
        }
        return result;
    }
    
    public void setChar(int index, char c) throws ArrayIndexOutOfBoundsException {
        setBoxedValue(index, Character.valueOf(c));
    }
    
    @Override
    public void setChars(int index, char[] b) throws ArrayIndexOutOfBoundsException {
        for (int i = 0; i < b.length; i++) {
            setChar(index + i, b[i]);
        }
    }

    public byte getByte(int index) throws ArrayIndexOutOfBoundsException {
        Object value = getBoxedValue(index);
        if (value instanceof Boolean) {
            return ((Boolean)value).booleanValue() ? (byte)1 : 0;
        } else {
            return ((Byte)value).byteValue();
        }   
    }

    @Override
    public byte[] getBytes(int index, int length) throws ArrayIndexOutOfBoundsException {
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = getByte(index + i);
        }
        return result;
    }
    
    public void setByte(int index, byte b) throws ArrayIndexOutOfBoundsException {
        setBoxedValue(index, Byte.valueOf(b));
    }

    @Override
    public void setBytes(int index, byte[] b) throws ArrayIndexOutOfBoundsException {
        for (int i = 0; i < b.length; i++) {
            setByte(index + i, b[i]);
        }
    }
    
    public boolean getBoolean(int index) throws ArrayIndexOutOfBoundsException {
        return ((Boolean)getBoxedValue(index)).booleanValue();
    }

    public void setBoolean(int index, boolean b) throws ArrayIndexOutOfBoundsException {
        setBoxedValue(index, Boolean.valueOf(b));
    }

    @Override
    public abstract Object clone() throws CloneNotSupportedException;
    
    @Override
    public void allowCollection(boolean flag) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isCollected() {
        throw new UnsupportedOperationException();
    }
}
