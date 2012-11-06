package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;

public class WrappingInstanceMirror extends WrappingMirror implements InstanceMirror {

    protected final InstanceMirror wrapped;
    
    public WrappingInstanceMirror(WrappingVirtualMachine vm, InstanceMirror wrappedInstance) {
        super(vm, wrappedInstance);
        this.wrapped = wrappedInstance;
    }

    
    @Override
    public ObjectMirror get(FieldMirror field) throws IllegalAccessException {
        return vm.getWrappedMirror(wrapped.get(vm.unwrapFieldMirror(field)));
    }

    @Override
    public boolean getBoolean(FieldMirror field) throws IllegalAccessException {
        return wrapped.getBoolean(vm.unwrapFieldMirror(field));
    }

    @Override
    public byte getByte(FieldMirror field) throws IllegalAccessException {
        return wrapped.getByte(vm.unwrapFieldMirror(field));
    }

    @Override
    public char getChar(FieldMirror field) throws IllegalAccessException {
        return wrapped.getChar(vm.unwrapFieldMirror(field));
    }

    @Override
    public short getShort(FieldMirror field) throws IllegalAccessException {
        return wrapped.getShort(vm.unwrapFieldMirror(field));
    }

    @Override
    public int getInt(FieldMirror field) throws IllegalAccessException {
        return wrapped.getInt(vm.unwrapFieldMirror(field));
    }

    @Override
    public long getLong(FieldMirror field) throws IllegalAccessException {
        return wrapped.getLong(vm.unwrapFieldMirror(field));
    }

    @Override
    public float getFloat(FieldMirror field) throws IllegalAccessException {
        return wrapped.getFloat(vm.unwrapFieldMirror(field));
    }

    @Override
    public double getDouble(FieldMirror field) throws IllegalAccessException {
        return wrapped.getDouble(vm.unwrapFieldMirror(field));
    }

    @Override
    public void set(FieldMirror field, ObjectMirror o) throws IllegalAccessException {
        wrapped.set(vm.unwrapFieldMirror(field), vm.unwrapMirror(o));
    }

    @Override
    public void setBoolean(FieldMirror field, boolean b) throws IllegalAccessException {
        wrapped.setBoolean(vm.unwrapFieldMirror(field), b);
    }

    @Override
    public void setByte(FieldMirror field, byte b) throws IllegalAccessException {
        wrapped.setByte(vm.unwrapFieldMirror(field), b);
    }

    @Override
    public void setChar(FieldMirror field, char c) throws IllegalAccessException {
        wrapped.setChar(vm.unwrapFieldMirror(field), c);
    }

    @Override
    public void setShort(FieldMirror field, short s) throws IllegalAccessException {
        wrapped.setShort(vm.unwrapFieldMirror(field), s);
    }

    @Override
    public void setInt(FieldMirror field, int i) throws IllegalAccessException {
        wrapped.setInt(vm.unwrapFieldMirror(field), i);
    }

    @Override
    public void setLong(FieldMirror field, long l) throws IllegalAccessException {
        wrapped.setLong(vm.unwrapFieldMirror(field), l);
    }

    @Override
    public void setFloat(FieldMirror field, float f) throws IllegalAccessException {
        wrapped.setFloat(vm.unwrapFieldMirror(field), f);
    }

    @Override
    public void setDouble(FieldMirror field, double d) throws IllegalAccessException {
        wrapped.setDouble(vm.unwrapFieldMirror(field), d);
    }
}
