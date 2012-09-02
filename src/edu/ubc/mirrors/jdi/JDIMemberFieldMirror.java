package edu.ubc.mirrors.jdi;

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ShortValue;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.ObjectMirror;

public class JDIMemberFieldMirror implements FieldMirror {

    private final JDIVirtualMachineMirror vm;
    private final Field field;
    private final ObjectReference obj;
    
    public JDIMemberFieldMirror(JDIVirtualMachineMirror vm, Field field, ObjectReference obj) {
        this.vm = vm;
        this.field = field;
        this.obj = obj;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JDIMemberFieldMirror)) {
            return false;
        }
        
        JDIMemberFieldMirror other = (JDIMemberFieldMirror)obj;
        return field.equals(other.field) && vm.equals(other.vm);
    }
    
    @Override
    public int hashCode() {
        return 11 * field.hashCode() * vm.hashCode();
    }
    
    @Override
    public String getName() {
        return field.name();
    }

    @Override
    public ClassMirror getType() {
        return vm.makeClassMirror(obj.referenceType().classObject());
    }

    @Override
    public ObjectMirror get() throws IllegalAccessException {
        return vm.makeMirror((ObjectReference)obj.getValue(field));
    }

    @Override
    public boolean getBoolean() throws IllegalAccessException {
        return ((BooleanValue)obj.getValue(field)).booleanValue();
    }

    @Override
    public byte getByte() throws IllegalAccessException {
        return ((ByteValue)obj.getValue(field)).byteValue();
    }

    @Override
    public char getChar() throws IllegalAccessException {
        return ((CharValue)obj.getValue(field)).charValue();
    }

    @Override
    public short getShort() throws IllegalAccessException {
        return ((ShortValue)obj.getValue(field)).shortValue();
    }

    @Override
    public int getInt() throws IllegalAccessException {
        return ((IntegerValue)obj.getValue(field)).intValue();
    }

    @Override
    public long getLong() throws IllegalAccessException {
        return ((LongValue)obj.getValue(field)).longValue();
    }

    @Override
    public float getFloat() throws IllegalAccessException {
        return ((FloatValue)obj.getValue(field)).floatValue();
    }

    @Override
    public double getDouble() throws IllegalAccessException {
        return ((DoubleValue)obj.getValue(field)).doubleValue();
    }

    @Override
    public void set(ObjectMirror o) throws IllegalAccessException {
        // Could implement this but holograms don't need it.
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBoolean(boolean b) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setByte(byte b) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setChar(char c) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setShort(short s) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setInt(int i) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLong(long l) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFloat(float f) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDouble(double d) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

}
