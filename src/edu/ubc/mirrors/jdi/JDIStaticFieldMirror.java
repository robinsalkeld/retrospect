package edu.ubc.mirrors.jdi;

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortValue;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.ObjectMirror;

public class JDIStaticFieldMirror implements FieldMirror {

    private final JDIVirtualMachineMirror vm;
    private final ReferenceType refType;
    private final Field field;
    
    public JDIStaticFieldMirror(JDIVirtualMachineMirror vm, ReferenceType refType, Field field) {
        this.vm = vm;
        this.refType = refType;
        this.field = field;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JDIStaticFieldMirror)) {
            return false;
        }
        
        JDIStaticFieldMirror other = (JDIStaticFieldMirror)obj;
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
        try {
            return vm.makeClassMirror(field.type());
        } catch (ClassNotLoadedException e) {
            // TODO-RS: how to deal with this?
            throw new RuntimeException(e);
        }
    }

    @Override
    public ObjectMirror get() throws IllegalAccessException {
        return vm.makeMirror((ObjectReference)refType.getValue(field));
    }

    @Override
    public boolean getBoolean() throws IllegalAccessException {
        return ((BooleanValue)refType.getValue(field)).booleanValue();
    }

    @Override
    public byte getByte() throws IllegalAccessException {
        return ((ByteValue)refType.getValue(field)).byteValue();
    }

    @Override
    public char getChar() throws IllegalAccessException {
        return ((CharValue)refType.getValue(field)).charValue();
    }

    @Override
    public short getShort() throws IllegalAccessException {
        return ((ShortValue)refType.getValue(field)).shortValue();
    }

    @Override
    public int getInt() throws IllegalAccessException {
        return ((IntegerValue)refType.getValue(field)).intValue();
    }

    @Override
    public long getLong() throws IllegalAccessException {
        return ((LongValue)refType.getValue(field)).longValue();
    }

    @Override
    public float getFloat() throws IllegalAccessException {
        return ((FloatValue)refType.getValue(field)).floatValue();
    }

    @Override
    public double getDouble() throws IllegalAccessException {
        return ((DoubleValue)refType.getValue(field)).doubleValue();
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
