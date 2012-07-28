package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
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
        if (vm instanceof VirtualMachineHolograph && wrapped.getType() instanceof HeapDumpClassMirror) {
            Breakpoint.bp();
        }
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
    public ObjectMirror get() throws IllegalAccessException {
        return vm.getWrappedMirror(wrapped.get());
    }

    @Override
    public boolean getBoolean() throws IllegalAccessException {
        return wrapped.getBoolean();
    }

    @Override
    public byte getByte() throws IllegalAccessException {
        return wrapped.getByte();
    }

    @Override
    public char getChar() throws IllegalAccessException {
        return wrapped.getChar();
    }

    @Override
    public short getShort() throws IllegalAccessException {
        return wrapped.getShort();
    }

    @Override
    public int getInt() throws IllegalAccessException {
        return wrapped.getInt();
    }

    @Override
    public long getLong() throws IllegalAccessException {
        return wrapped.getLong();
    }

    @Override
    public float getFloat() throws IllegalAccessException {
        return wrapped.getFloat();
    }

    @Override
    public double getDouble() throws IllegalAccessException {
        return wrapped.getDouble();
    }

    @Override
    public void set(ObjectMirror o) throws IllegalAccessException {
        wrapped.set(vm.unwrapMirror(o));
    }

    @Override
    public void setBoolean(boolean b) throws IllegalAccessException {
        wrapped.setBoolean(b);
    }

    @Override
    public void setByte(byte b) throws IllegalAccessException {
        wrapped.setByte(b);
    }

    @Override
    public void setChar(char c) throws IllegalAccessException {
        wrapped.setChar(c);
    }

    @Override
    public void setShort(short s) throws IllegalAccessException {
        wrapped.setShort(s);
    }

    @Override
    public void setInt(int i) throws IllegalAccessException {
        wrapped.setInt(i);
    }

    @Override
    public void setLong(long l) throws IllegalAccessException {
        wrapped.setLong(l);
    }

    @Override
    public void setFloat(float f) throws IllegalAccessException {
        wrapped.setFloat(f);
    }

    @Override
    public void setDouble(double d) throws IllegalAccessException {
        wrapped.setDouble(d);
    }
}
