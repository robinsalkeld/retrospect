package edu.ubc.mirrors.mutable;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.ObjectMirror;

public class MutableFieldMirror implements FieldMirror {

    private final MutableVirtualMachineMirror vm;
    
    private final FieldMirror immutableFieldMirror;
    private Object newValue;
    private boolean initialized = false;
    
    public MutableFieldMirror(MutableVirtualMachineMirror vm, FieldMirror immutableFieldMirror) {
        this.vm = vm;
        this.immutableFieldMirror = immutableFieldMirror;
    }
    
    @Override
    public String getName() {
        return immutableFieldMirror.getName();
    }
    
    @Override
    public ClassMirror getType() {
        return immutableFieldMirror.getType();
    }
    
    @Override
    public ObjectMirror get() throws IllegalAccessException {
        if (!initialized) {
            newValue = vm.getWrappedMirror(immutableFieldMirror.get());
            initialized = true;
        }
        return (ObjectMirror)newValue;
    }

    @Override
    public boolean getBoolean() throws IllegalAccessException {
        return (initialized ? (Boolean)newValue : immutableFieldMirror.getBoolean());
    }

    @Override
    public byte getByte() throws IllegalAccessException {
        return (initialized ? (Byte)newValue : immutableFieldMirror.getByte());
    }

    @Override
    public char getChar() throws IllegalAccessException {
        return (initialized ? (Character)newValue : immutableFieldMirror.getChar());
    }

    @Override
    public short getShort() throws IllegalAccessException {
        return (initialized ? (Short)newValue : immutableFieldMirror.getShort());
    }

    @Override
    public int getInt() throws IllegalAccessException {
        return (initialized ? (Integer)newValue : immutableFieldMirror.getInt());
    }

    @Override
    public long getLong() throws IllegalAccessException {
        return (initialized ? (Long)newValue : immutableFieldMirror.getLong());
    }

    @Override
    public float getFloat() throws IllegalAccessException {
        return (initialized ? (Float)newValue : immutableFieldMirror.getFloat());
    }

    @Override
    public double getDouble() throws IllegalAccessException {
        return (initialized ? (Double)newValue : immutableFieldMirror.getDouble());
    }

    @Override
    public void set(ObjectMirror o) throws IllegalAccessException {
        newValue = o;
        initialized = true;
    }

    @Override
    public void setBoolean(boolean b) throws IllegalAccessException {
        newValue = b;
        initialized = true;
    }

    @Override
    public void setByte(byte b) throws IllegalAccessException {
        newValue = b;
        initialized = true;
    }

    @Override
    public void setChar(char c) throws IllegalAccessException {
        newValue = c;
        initialized = true;
    }

    @Override
    public void setShort(short s) throws IllegalAccessException {
        newValue = s;
        initialized = true;
    }

    @Override
    public void setInt(int i) throws IllegalAccessException {
        newValue = i;
        initialized = true;
    }

    @Override
    public void setLong(long l) throws IllegalAccessException {
        newValue = l;
        initialized = true;
    }

    @Override
    public void setFloat(float f) throws IllegalAccessException {
        newValue = f;
        initialized = true;
    }

    @Override
    public void setDouble(double d) throws IllegalAccessException {
        newValue = d;
        initialized = true;
    }

}
