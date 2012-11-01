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
import com.sun.jdi.ShortValue;
import com.sun.jdi.Value;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;

public class JDIFieldMirror extends JDIMirror implements FieldMirror {

    private final Field field;
    
    public JDIFieldMirror(JDIVirtualMachineMirror vm, Field field) {
	super(vm, field);
        this.field = field;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JDIFieldMirror)) {
            return false;
        }
        
        JDIFieldMirror other = (JDIFieldMirror)obj;
        return field.equals(other.field) && vm.equals(other.vm);
    }
    
    @Override
    public int hashCode() {
        return 11 * field.hashCode() * vm.hashCode();
    }
    
    @Override
    public ClassMirror getDeclaringClass() {
        return vm.makeClassMirror(field.declaringType());
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
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public int getModifiers() {
        return field.modifiers();
    }
    
    private Value getValue(InstanceMirror obj) {
        if (field.isStatic()) {
            return field.declaringType().getValue(field);
        } else if (obj instanceof ClassMirror) {
            return null;
        } else {
            return ((JDIInstanceMirror)obj).mirror.getValue(field);
        }
    }
    
    @Override
    public ObjectMirror get(InstanceMirror obj) throws IllegalAccessException {
        Value value = getValue(obj);
        return value == null ? null : vm.makeMirror((ObjectReference)value);
    }

    @Override
    public boolean getBoolean(InstanceMirror obj) throws IllegalAccessException {
        Value value = getValue(obj);
        return value == null ? false : ((BooleanValue)getValue(obj)).booleanValue();
    }

    @Override
    public byte getByte(InstanceMirror obj) throws IllegalAccessException {
        Value value = getValue(obj);
        return value == null ? 0 : ((ByteValue)getValue(obj)).byteValue();
    }

    @Override
    public char getChar(InstanceMirror obj) throws IllegalAccessException {
        Value value = getValue(obj);
        return value == null ? 0 : ((CharValue)getValue(obj)).charValue();
    }

    @Override
    public short getShort(InstanceMirror obj) throws IllegalAccessException {
        Value value = getValue(obj);
        return value == null ? 0 : ((ShortValue)getValue(obj)).shortValue();
    }

    @Override
    public int getInt(InstanceMirror obj) throws IllegalAccessException {
        Value value = getValue(obj);
        return value == null ? 0 : ((IntegerValue)getValue(obj)).intValue();
    }

    @Override
    public long getLong(InstanceMirror obj) throws IllegalAccessException {
        Value value = getValue(obj);
        return value == null ? 0 : ((LongValue)getValue(obj)).longValue();
    }

    @Override
    public float getFloat(InstanceMirror obj) throws IllegalAccessException {
        Value value = getValue(obj);
        return value == null ? 0 : ((FloatValue)getValue(obj)).floatValue();
    }

    @Override
    public double getDouble(InstanceMirror obj) throws IllegalAccessException {
        Value value = getValue(obj);
        return value == null ? 0 : ((DoubleValue)getValue(obj)).doubleValue();
    }

    @Override
    public void set(InstanceMirror obj, ObjectMirror o) throws IllegalAccessException {
        // Could implement this but holograms don't need it.
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBoolean(InstanceMirror obj, boolean b) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setByte(InstanceMirror obj, byte b) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setChar(InstanceMirror obj, char c) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setShort(InstanceMirror obj, short s) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setInt(InstanceMirror obj, int i) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLong(InstanceMirror obj, long l) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFloat(InstanceMirror obj, float f) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDouble(InstanceMirror obj, double d) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

}
