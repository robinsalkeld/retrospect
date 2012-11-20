package edu.ubc.mirrors.jdi;

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ShortValue;
import com.sun.jdi.Value;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;

public class JDIInstanceMirror extends JDIObjectMirror implements InstanceMirror {
    
    public JDIInstanceMirror(JDIVirtualMachineMirror vm, ObjectReference t) {
        super(vm, t);
    }

    protected Value getValue(FieldMirror field) {
        return mirror.getValue(((JDIFieldMirror)field).field);
    }
    
    @Override
    public ObjectMirror get(FieldMirror field) throws IllegalAccessException {
        Value value = getValue(field);
        return vm.makeMirror((ObjectReference)value);
    }

    @Override
    public boolean getBoolean(FieldMirror field) throws IllegalAccessException {
        Value value = getValue(field);
        return value == null ? false : ((BooleanValue)value).booleanValue();
    }

    @Override
    public byte getByte(FieldMirror field) throws IllegalAccessException {
        Value value = getValue(field);
        return value == null ? 0 : ((ByteValue)value).byteValue();
    }

    @Override
    public char getChar(FieldMirror field) throws IllegalAccessException {
        Value value = getValue(field);
        return value == null ? 0 : ((CharValue)value).charValue();
    }

    @Override
    public short getShort(FieldMirror field) throws IllegalAccessException {
        Value value = getValue(field);
        return value == null ? 0 : ((ShortValue)value).shortValue();
    }

    @Override
    public int getInt(FieldMirror field) throws IllegalAccessException {
        Value value = getValue(field);
        return value == null ? 0 : ((IntegerValue)value).intValue();
    }

    @Override
    public long getLong(FieldMirror field) throws IllegalAccessException {
        Value value = getValue(field);
        return value == null ? 0 : ((LongValue)value).longValue();
    }

    @Override
    public float getFloat(FieldMirror field) throws IllegalAccessException {
        Value value = getValue(field);
        return value == null ? 0 : ((FloatValue)value).floatValue();
    }

    @Override
    public double getDouble(FieldMirror field) throws IllegalAccessException {
        Value value = getValue(field);
        return value == null ? 0 : ((DoubleValue)value).doubleValue();
    }

    @Override
    public void set(FieldMirror field, ObjectMirror o) throws IllegalAccessException {
        // Could implement this but holograms don't need it.
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBoolean(FieldMirror field, boolean b) throws IllegalAccessException {
        // Could implement this but holograms don't need it.
        throw new UnsupportedOperationException();
    }

    @Override
    public void setByte(FieldMirror field, byte b) throws IllegalAccessException {
        // Could implement this but holograms don't need it.
        throw new UnsupportedOperationException();
    }

    @Override
    public void setChar(FieldMirror field, char c) throws IllegalAccessException {
        // Could implement this but holograms don't need it.
        throw new UnsupportedOperationException();
    }

    @Override
    public void setShort(FieldMirror field, short s) throws IllegalAccessException {
        // Could implement this but holograms don't need it.
        throw new UnsupportedOperationException();
    }

    @Override
    public void setInt(FieldMirror field, int i) throws IllegalAccessException {
        // Could implement this but holograms don't need it.
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLong(FieldMirror field, long l) throws IllegalAccessException {
        // Could implement this but holograms don't need it.
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFloat(FieldMirror field, float f) throws IllegalAccessException {
        // Could implement this but holograms don't need it.
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDouble(FieldMirror field, double d) throws IllegalAccessException {
        // Could implement this but holograms don't need it.
        throw new UnsupportedOperationException();
    }

}
