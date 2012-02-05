package edu.ubc.mirrors;

public abstract class BoxingFieldMirror implements FieldMirror {

    public abstract Object get() throws IllegalAccessException;
    
    public boolean getBoolean() throws IllegalAccessException {
        Object value = get();
        return value == null ? false : ((Boolean)value).booleanValue();
    }

    public byte getByte() throws IllegalAccessException {
        Object value = get();
        return value == null ? 0 : ((Byte)value).byteValue();
    }
    
    public char getChar() throws IllegalAccessException {
        Object value = get();
        return value == null ? 0 : ((Character)value).charValue();
    }

    public short getShort() throws IllegalAccessException {
        Object value = get();
        return value == null ? 0 : ((Byte)value).byteValue();
    }
    
    public int getInt() throws IllegalAccessException {
        Object value = get();
        return value == null ? 0 : ((Integer)value).intValue();
    }
    
    public long getLong() throws IllegalAccessException {
        Object value = get();
        return value == null ? 0 : ((Long)value).longValue();
    }

    public float getFloat() throws IllegalAccessException {
        Object value = get();
        return value == null ? 0 : ((Float)value).floatValue();
    }

    public double getDouble() throws IllegalAccessException {
        Object value = get();
        return value == null ? 0 : ((Double)value).doubleValue();
    }

    public abstract void set(Object o) throws IllegalAccessException;

    public void setBoolean(boolean b) throws IllegalAccessException {
        set(Boolean.valueOf(b));
    }

    public void setByte(byte b) throws IllegalAccessException {
        set(Byte.valueOf(b));
    }

    public void setChar(char c) throws IllegalAccessException {
        set(Character.valueOf(c));
    }

    public void setShort(short s) throws IllegalAccessException {
        set(Short.valueOf(s));
    }
    
    public void setInt(int i) throws IllegalAccessException {
        set(Integer.valueOf(i));
    }

    public void setLong(long l) throws IllegalAccessException {
        set(Long.valueOf(l));
    }
    
    public void setFloat(float f) throws IllegalAccessException {
        set(Float.valueOf(f));
    }
    
    public void setDouble(double d) throws IllegalAccessException {
        set(Double.valueOf(d));
    }
}
