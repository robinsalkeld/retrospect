package edu.ubc.mirrors;

public interface FieldMirror {

    public Object get() throws IllegalAccessException;
    public boolean getBoolean() throws IllegalAccessException;
    public byte getByte() throws IllegalAccessException;
    public char getChar() throws IllegalAccessException;
    public short getShort() throws IllegalAccessException;
    public int getInt() throws IllegalAccessException;
    public long getLong() throws IllegalAccessException;
    public float getFloat() throws IllegalAccessException;
    public double getDouble() throws IllegalAccessException;
    
    public void set(Object o) throws IllegalAccessException;
    public void setBoolean(boolean b) throws IllegalAccessException;
    public void setByte(byte b) throws IllegalAccessException;
    public void setChar(char c) throws IllegalAccessException;
    public void setShort(short s) throws IllegalAccessException;
    public void setInt(int i) throws IllegalAccessException;
    public void setLong(long l) throws IllegalAccessException;
    public void setFloat(float f) throws IllegalAccessException;
    public void setDouble(double d) throws IllegalAccessException;
    
}
