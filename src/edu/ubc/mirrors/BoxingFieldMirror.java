package edu.ubc.mirrors;

public abstract class BoxingFieldMirror implements FieldMirror {

    public abstract Object getBoxedValue() throws IllegalAccessException;
    
    public boolean getBoolean() throws IllegalAccessException {
        Object value = getBoxedValue();
        return value == null ? false : ((Boolean)value).booleanValue();
    }

    public byte getByte() throws IllegalAccessException {
        Object value = getBoxedValue();
        return value == null ? 0 : ((Byte)value).byteValue();
    }
    
    public char getChar() throws IllegalAccessException {
        Object value = getBoxedValue();
        return value == null ? 0 : ((Character)value).charValue();
    }

    public short getShort() throws IllegalAccessException {
        Object value = getBoxedValue();
        return value == null ? 0 : ((Short)value).byteValue();
    }
    
    public int getInt() throws IllegalAccessException {
        Object value = getBoxedValue();
        return value == null ? 0 : ((Integer)value).intValue();
    }
    
    public long getLong() throws IllegalAccessException {
        Object value = getBoxedValue();
        return value == null ? 0 : ((Long)value).longValue();
    }

    public float getFloat() throws IllegalAccessException {
        Object value = getBoxedValue();
        return value == null ? 0 : ((Float)value).floatValue();
    }

    public double getDouble() throws IllegalAccessException {
        Object value = getBoxedValue();
        return value == null ? 0 : ((Double)value).doubleValue();
    }

    public abstract void setBoxedValue(Object o) throws IllegalAccessException;

    public void setBoolean(boolean b) throws IllegalAccessException {
        setBoxedValue(Boolean.valueOf(b));
    }

    public void setByte(byte b) throws IllegalAccessException {
        setBoxedValue(Byte.valueOf(b));
    }

    public void setChar(char c) throws IllegalAccessException {
        setBoxedValue(Character.valueOf(c));
    }

    public void setShort(short s) throws IllegalAccessException {
        setBoxedValue(Short.valueOf(s));
    }
    
    public void setInt(int i) throws IllegalAccessException {
        setBoxedValue(Integer.valueOf(i));
    }

    public void setLong(long l) throws IllegalAccessException {
        setBoxedValue(Long.valueOf(l));
    }
    
    public void setFloat(float f) throws IllegalAccessException {
        setBoxedValue(Float.valueOf(f));
    }
    
    public void setDouble(double d) throws IllegalAccessException {
        setBoxedValue(Double.valueOf(d));
    }
}
