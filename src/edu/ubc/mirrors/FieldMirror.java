package edu.ubc.mirrors;

public interface FieldMirror {

    public ClassMirror getDeclaringClass();
    public String getName();
    public ClassMirror getType();
    public int getModifiers();
    
    public ObjectMirror get(InstanceMirror obj) throws IllegalAccessException;
    public boolean getBoolean(InstanceMirror obj) throws IllegalAccessException;
    public byte getByte(InstanceMirror obj) throws IllegalAccessException;
    public char getChar(InstanceMirror obj) throws IllegalAccessException;
    public short getShort(InstanceMirror obj) throws IllegalAccessException;
    public int getInt(InstanceMirror obj) throws IllegalAccessException;
    public long getLong(InstanceMirror obj) throws IllegalAccessException;
    public float getFloat(InstanceMirror obj) throws IllegalAccessException;
    public double getDouble(InstanceMirror obj) throws IllegalAccessException;
    
    public void set(InstanceMirror obj, ObjectMirror o) throws IllegalAccessException;
    public void setBoolean(InstanceMirror obj, boolean b) throws IllegalAccessException;
    public void setByte(InstanceMirror obj, byte b) throws IllegalAccessException;
    public void setChar(InstanceMirror obj, char c) throws IllegalAccessException;
    public void setShort(InstanceMirror obj, short s) throws IllegalAccessException;
    public void setInt(InstanceMirror obj, int i) throws IllegalAccessException;
    public void setLong(InstanceMirror obj, long l) throws IllegalAccessException;
    public void setFloat(InstanceMirror obj, float f) throws IllegalAccessException;
    public void setDouble(InstanceMirror obj, double d) throws IllegalAccessException;
    
}
