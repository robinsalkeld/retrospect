package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpClassMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.test.Breakpoint;

public class WrappingFieldMirror implements FieldMirror {

    private final WrappingVirtualMachine vm;
    protected final FieldMirror wrapped;
    
    protected WrappingFieldMirror(WrappingVirtualMachine vm, FieldMirror wrapped) {
        this.vm = vm;
        this.wrapped = wrapped;
    }
    
    @Override
    public ClassMirror getDeclaringClass() {
        return vm.getWrappedClassMirror(wrapped.getDeclaringClass());
    }
    
    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public ClassMirror getType() {
        return vm.getWrappedClassMirror(wrapped.getType());
    }

    @Override
    public int getModifiers() {
        return wrapped.getModifiers();
    }
    
    @Override
    public ObjectMirror get(InstanceMirror obj) throws IllegalAccessException {
        return vm.getWrappedMirror(wrapped.get(vm.unwrapInstanceMirror(obj)));
    }

    @Override
    public boolean getBoolean(InstanceMirror obj) throws IllegalAccessException {
        return wrapped.getBoolean(vm.unwrapInstanceMirror(obj));
    }

    @Override
    public byte getByte(InstanceMirror obj) throws IllegalAccessException {
        return wrapped.getByte(vm.unwrapInstanceMirror(obj));
    }

    @Override
    public char getChar(InstanceMirror obj) throws IllegalAccessException {
        return wrapped.getChar(vm.unwrapInstanceMirror(obj));
    }

    @Override
    public short getShort(InstanceMirror obj) throws IllegalAccessException {
        return wrapped.getShort(vm.unwrapInstanceMirror(obj));
    }

    @Override
    public int getInt(InstanceMirror obj) throws IllegalAccessException {
        return wrapped.getInt(vm.unwrapInstanceMirror(obj));
    }

    @Override
    public long getLong(InstanceMirror obj) throws IllegalAccessException {
        return wrapped.getLong(vm.unwrapInstanceMirror(obj));
    }

    @Override
    public float getFloat(InstanceMirror obj) throws IllegalAccessException {
        return wrapped.getFloat(vm.unwrapInstanceMirror(obj));
    }

    @Override
    public double getDouble(InstanceMirror obj) throws IllegalAccessException {
        return wrapped.getDouble(vm.unwrapInstanceMirror(obj));
    }

    @Override
    public void set(InstanceMirror obj, ObjectMirror o) throws IllegalAccessException {
        wrapped.set(vm.unwrapInstanceMirror(obj), vm.unwrapMirror(o));
    }

    @Override
    public void setBoolean(InstanceMirror obj, boolean b) throws IllegalAccessException {
        wrapped.setBoolean(vm.unwrapInstanceMirror(obj), b);
    }

    @Override
    public void setByte(InstanceMirror obj, byte b) throws IllegalAccessException {
        wrapped.setByte(vm.unwrapInstanceMirror(obj), b);
    }

    @Override
    public void setChar(InstanceMirror obj, char c) throws IllegalAccessException {
        wrapped.setChar(vm.unwrapInstanceMirror(obj), c);
    }

    @Override
    public void setShort(InstanceMirror obj, short s) throws IllegalAccessException {
        wrapped.setShort(vm.unwrapInstanceMirror(obj), s);
    }

    @Override
    public void setInt(InstanceMirror obj, int i) throws IllegalAccessException {
        wrapped.setInt(vm.unwrapInstanceMirror(obj), i);
    }

    @Override
    public void setLong(InstanceMirror obj, long l) throws IllegalAccessException {
        wrapped.setLong(vm.unwrapInstanceMirror(obj), l);
    }

    @Override
    public void setFloat(InstanceMirror obj, float f) throws IllegalAccessException {
        wrapped.setFloat(vm.unwrapInstanceMirror(obj), f);
    }

    @Override
    public void setDouble(InstanceMirror obj, double d) throws IllegalAccessException {
        wrapped.setDouble(vm.unwrapInstanceMirror(obj), d);
    }
}
