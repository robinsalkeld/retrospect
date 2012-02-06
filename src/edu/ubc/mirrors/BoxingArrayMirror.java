package edu.ubc.mirrors;

public abstract class BoxingArrayMirror implements ObjectArrayMirror,
                                          BooleanArrayMirror, ByteArrayMirror, CharArrayMirror, ShortArrayMirror,
                                          IntArrayMirror, LongArrayMirror, FloatArrayMirror, DoubleArrayMirror {

    public double getDouble(int index) throws ArrayIndexOutOfBoundsException {
        return ((Double)get(index)).doubleValue();
    }

    public void setDouble(int index, double d) throws ArrayIndexOutOfBoundsException {
        set(index, Double.valueOf(d));
    }

    public float getFloat(int index) throws ArrayIndexOutOfBoundsException {
        return ((Float)get(index)).floatValue();
    }

    public void setFloat(int index, float f) throws ArrayIndexOutOfBoundsException {
        set(index, Float.valueOf(f));
    }

    public long getLong(int index) throws ArrayIndexOutOfBoundsException {
        return ((Long)get(index)).longValue();
    }

    public void setLong(int index, long l) throws ArrayIndexOutOfBoundsException {
        set(index, Long.valueOf(l));
    }

    public int getInt(int index) throws ArrayIndexOutOfBoundsException {
        return ((Integer)get(index)).intValue();
    }

    public void setInt(int index, int i) throws ArrayIndexOutOfBoundsException {
        set(index, Integer.valueOf(i));
    }

    public short getShort(int index) throws ArrayIndexOutOfBoundsException {
        return ((Short)get(index)).shortValue();
    }

    public void setShort(int index, short b) throws ArrayIndexOutOfBoundsException {
        set(index, Short.valueOf(b));
    }

    public char getChar(int index) throws ArrayIndexOutOfBoundsException {
        return ((Character)get(index)).charValue();
    }

    public void setChar(int index, char c) throws ArrayIndexOutOfBoundsException {
        set(index, Character.valueOf(c));
    }

    public byte getByte(int index) throws ArrayIndexOutOfBoundsException {
        return ((Byte)get(index)).byteValue();
    }

    public void setByte(int index, byte b) throws ArrayIndexOutOfBoundsException {
        set(index, Byte.valueOf(b));
    }

    public boolean getBoolean(int index) throws ArrayIndexOutOfBoundsException {
        return ((Boolean)get(index)).booleanValue();
    }

    public void setBoolean(int index, boolean b) throws ArrayIndexOutOfBoundsException {
        set(index, Boolean.valueOf(b));
    }

}
