package edu.ubc.mirrors.holographs;

import java.util.HashMap;
import java.util.Map;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.wrapping.WrappingFieldMirror;
import edu.ubc.mirrors.wrapping.WrappingInstanceMirror;

public class MutableFieldMirror extends WrappingFieldMirror {

    private final ClassHolograph klass;
    private final FieldMirror immutableFieldMirror;
    // Note null keys are used for static fields.
    // TODO-RS: There is probably a much more efficient implementation of this,
    // but this way I don't have to assume only one class of InstanceMirror.
    private Map<InstanceMirror, Object> newValues = new HashMap<InstanceMirror, Object>();
    
    private FieldMirror bytecodeField;
    
    public MutableFieldMirror(ClassHolograph klass, FieldMirror immutableFieldMirror) {
        super(klass.getVM(), immutableFieldMirror);
        this.klass = klass;
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
    public int getModifiers() {
        try {
            return immutableFieldMirror.getModifiers();
        } catch (UnsupportedOperationException e) {
            return getBytecodeField().getModifiers();
        }
    }
    
    private FieldMirror getBytecodeField() {
        if (bytecodeField == null) {
            try {
                bytecodeField = klass.getBytecodeMirror().getDeclaredField(wrapped.getName());
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        return bytecodeField;
    }
    
    private boolean isNewInstance(InstanceMirror obj) {
        if (obj == null) {
            // Static field
            return isNewInstance(getDeclaringClass());
        } else {
            return ((WrappingInstanceMirror)obj).getWrapped() instanceof NewHolographInstance;
        }
    }
    
    @Override
    public ObjectMirror get(InstanceMirror obj) throws IllegalAccessException {
        if (newValues.containsKey(obj)) {
            return (ObjectMirror)newValues.get(obj);
        } else if (isNewInstance(obj)) {
            return null;
        } else {
            return super.get(obj);
        }
    }

    @Override
    public boolean getBoolean(InstanceMirror obj) throws IllegalAccessException {
        if (newValues.containsKey(obj)) {
            return (Boolean)newValues.get(obj);
        } else if (isNewInstance(obj)) {
            return false;
        } else {
            return super.getBoolean(obj);
        }
    }

    @Override
    public byte getByte(InstanceMirror obj) throws IllegalAccessException {
        if (newValues.containsKey(obj)) {
            return (Byte)newValues.get(obj);
        } else if (isNewInstance(obj)) {
            return 0;
        } else {
            return super.getByte(obj);
        }
    }

    @Override
    public char getChar(InstanceMirror obj) throws IllegalAccessException {
        if (newValues.containsKey(obj)) {
            return (Character)newValues.get(obj);
        } else if (isNewInstance(obj)) {
            return 0;
        } else {
            return super.getChar(obj);
        }
    }

    @Override
    public short getShort(InstanceMirror obj) throws IllegalAccessException {
        if (newValues.containsKey(obj)) {
            return (Short)newValues.get(obj);
        } else if (isNewInstance(obj)) {
            return 0;
        } else {
            return super.getShort(obj);
        }
    }

    @Override
    public int getInt(InstanceMirror obj) throws IllegalAccessException {
        if (newValues.containsKey(obj)) {
            return (Integer)newValues.get(obj);
        } else if (isNewInstance(obj)) {
            return 0;
        } else {
            return super.getInt(obj);
        }
    }

    @Override
    public long getLong(InstanceMirror obj) throws IllegalAccessException {
        if (newValues.containsKey(obj)) {
            return (Long)newValues.get(obj);
        } else if (isNewInstance(obj)) {
            return 0;
        } else {
            return super.getLong(obj);
        }
    }

    @Override
    public float getFloat(InstanceMirror obj) throws IllegalAccessException {
        if (newValues.containsKey(obj)) {
            return (Float)newValues.get(obj);
        } else if (isNewInstance(obj)) {
            return 0;
        } else {
            return super.getFloat(obj);
        }
    }

    @Override
    public double getDouble(InstanceMirror obj) throws IllegalAccessException {
        if (newValues.containsKey(obj)) {
            return (Double)newValues.get(obj);
        } else if (isNewInstance(obj)) {
            return 0;
        } else {
            return super.getDouble(obj);
        }
    }

    @Override
    public void set(InstanceMirror obj, ObjectMirror o) throws IllegalAccessException {
        newValues.put(obj, o);
    }

    @Override
    public void setBoolean(InstanceMirror obj, boolean b) throws IllegalAccessException {
        newValues.put(obj, b);
    }

    @Override
    public void setByte(InstanceMirror obj, byte b) throws IllegalAccessException {
        newValues.put(obj, b);
    }

    @Override
    public void setChar(InstanceMirror obj, char c) throws IllegalAccessException {
        newValues.put(obj, c);
    }

    @Override
    public void setShort(InstanceMirror obj, short s) throws IllegalAccessException {
        newValues.put(obj, s);
    }

    @Override
    public void setInt(InstanceMirror obj, int i) throws IllegalAccessException {
        newValues.put(obj, i);
    }

    @Override
    public void setLong(InstanceMirror obj, long l) throws IllegalAccessException {
        newValues.put(obj, l);
    }

    @Override
    public void setFloat(InstanceMirror obj, float f) throws IllegalAccessException {
        newValues.put(obj, f);
    }

    @Override
    public void setDouble(InstanceMirror obj, double d) throws IllegalAccessException {
        newValues.put(obj, d);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + immutableFieldMirror;
    }

}
