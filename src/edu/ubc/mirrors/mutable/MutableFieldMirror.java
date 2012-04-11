package edu.ubc.mirrors.mutable;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.ObjectMirror;

public class MutableFieldMirror implements FieldMirror {

    private final MutableVirtualMachineMirror vm;
    
    private final FieldMirror immutableFieldMirror;
    private final FieldMirror mutableLayer;
    private boolean initialized;
    
    public MutableFieldMirror(MutableVirtualMachineMirror vm, FieldMirror mutableLayer, FieldMirror immutableFieldMirror) {
        this.vm = vm;
        this.mutableLayer = mutableLayer;
        this.immutableFieldMirror = immutableFieldMirror;
    }
    
    @Override
    public String getName() {
        return immutableFieldMirror.getName();
    }
    
    @Override
    public Class<?> getType() {
        return immutableFieldMirror.getType();
    }
    
    @Override
    public ObjectMirror get() throws IllegalAccessException {
        if (!initialized) {
            mutableLayer.set(vm.makeMirror(immutableFieldMirror.get()));
            initialized = true;
        }
        return mutableLayer.get();
    }

    @Override
    public boolean getBoolean() throws IllegalAccessException {
        return (initialized ? mutableLayer.getBoolean() : immutableFieldMirror.getBoolean());
    }

    @Override
    public byte getByte() throws IllegalAccessException {
        return (initialized ? mutableLayer.getByte() : immutableFieldMirror.getByte());
    }

    @Override
    public char getChar() throws IllegalAccessException {
        return (initialized ? mutableLayer.getChar() : immutableFieldMirror.getChar());
    }

    @Override
    public short getShort() throws IllegalAccessException {
        return (initialized ? mutableLayer.getShort() : immutableFieldMirror.getShort());
    }

    @Override
    public int getInt() throws IllegalAccessException {
        return (initialized ? mutableLayer.getInt() : immutableFieldMirror.getInt());
    }

    @Override
    public long getLong() throws IllegalAccessException {
        return (initialized ? mutableLayer.getLong() : immutableFieldMirror.getLong());
    }

    @Override
    public float getFloat() throws IllegalAccessException {
        return (initialized ? mutableLayer.getFloat() : immutableFieldMirror.getFloat());
    }

    @Override
    public double getDouble() throws IllegalAccessException {
        return (initialized ? mutableLayer.getDouble() : immutableFieldMirror.getDouble());
    }

    @Override
    public void set(ObjectMirror o) throws IllegalAccessException {
        mutableLayer.set(o);
        initialized = true;
    }

    @Override
    public void setBoolean(boolean b) throws IllegalAccessException {
        mutableLayer.setBoolean(b);
        initialized = true;
    }

    @Override
    public void setByte(byte b) throws IllegalAccessException {
        mutableLayer.setByte(b);
        initialized = true;
    }

    @Override
    public void setChar(char c) throws IllegalAccessException {
        mutableLayer.setChar(c);
        initialized = true;
    }

    @Override
    public void setShort(short s) throws IllegalAccessException {
        mutableLayer.setShort(s);
        initialized = true;
    }

    @Override
    public void setInt(int i) throws IllegalAccessException {
        mutableLayer.setInt(i);
        initialized = true;
    }

    @Override
    public void setLong(long l) throws IllegalAccessException {
        mutableLayer.setLong(l);
        initialized = true;
    }

    @Override
    public void setFloat(float f) throws IllegalAccessException {
        mutableLayer.setFloat(f);
        initialized = true;
    }

    @Override
    public void setDouble(double d) throws IllegalAccessException {
        mutableLayer.setDouble(d);
        initialized = true;
    }

}
