package edu.ubc.mirrors;


public interface InstanceMirror extends ObjectMirror {

    public ObjectMirror get(FieldMirror field) throws IllegalAccessException;
    public boolean getBoolean(FieldMirror field) throws IllegalAccessException;
    public byte getByte(FieldMirror field) throws IllegalAccessException;
    public char getChar(FieldMirror field) throws IllegalAccessException;
    public short getShort(FieldMirror field) throws IllegalAccessException;
    public int getInt(FieldMirror field) throws IllegalAccessException;
    public long getLong(FieldMirror field) throws IllegalAccessException;
    public float getFloat(FieldMirror field) throws IllegalAccessException;
    public double getDouble(FieldMirror field) throws IllegalAccessException;
    
    public void set(FieldMirror field, ObjectMirror o) throws IllegalAccessException;
    public void setBoolean(FieldMirror field, boolean b) throws IllegalAccessException;
    public void setByte(FieldMirror field, byte b) throws IllegalAccessException;
    public void setChar(FieldMirror field, char c) throws IllegalAccessException;
    public void setShort(FieldMirror field, short s) throws IllegalAccessException;
    public void setInt(FieldMirror field, int i) throws IllegalAccessException;
    public void setLong(FieldMirror field, long l) throws IllegalAccessException;
    public void setFloat(FieldMirror field, float f) throws IllegalAccessException;
    public void setDouble(FieldMirror field, double d) throws IllegalAccessException;
}
