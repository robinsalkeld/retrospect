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

    public void setChar(int index, char c) throws ArrayIndexOutOfBoundsException {
        setBoxedValue(index, Character.valueOf(c));
    }

    public byte getByte(int index) throws ArrayIndexOutOfBoundsException {
        Object value = getBoxedValue(index);
        if (value instanceof Boolean) {
            return ((Boolean)value).booleanValue() ? (byte)1 : 0;
        } else {
            return ((Byte)value).byteValue();
        }   
    }

    public void setByte(int index, byte b) throws ArrayIndexOutOfBoundsException {
        setBoxedValue(index, Byte.valueOf(b));
    }

    public boolean getBoolean(int index) throws ArrayIndexOutOfBoundsException {
        return ((Boolean)getBoxedValue(index)).booleanValue();
    }

    public void setBoolean(int index, boolean b) throws ArrayIndexOutOfBoundsException {
        setBoxedValue(index, Boolean.valueOf(b));
    }

}
