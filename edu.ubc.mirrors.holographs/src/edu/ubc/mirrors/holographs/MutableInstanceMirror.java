package edu.ubc.mirrors.holographs;

import java.util.HashMap;
import java.util.Map;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;

public class MutableInstanceMirror implements InstanceMirror {
    
    private final InstanceMirror wrapped;
    private final Map<FieldMirror, Object> newValues = new HashMap<FieldMirror, Object>();
    
    public MutableInstanceMirror(InstanceMirror wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ClassMirror getClassMirror() {
        return wrapped.getClassMirror();
    }
    
    @Override
    public ObjectMirror get(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (ObjectMirror)newValues.get(field);
        } else {
            return wrapped.get(field);
        }
    }
    
    @Override
    public boolean getBoolean(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Boolean)newValues.get(field);
        } else {
            return wrapped.getBoolean(field);
        }
    }
    
    @Override
    public byte getByte(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Byte)newValues.get(field);
        } else {
            return wrapped.getByte(field);
        }
    }
    
    @Override
    public char getChar(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Character)newValues.get(field);
        } else {
            return wrapped.getChar(field);
        }
    }
    
    @Override
    public short getShort(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Short)newValues.get(field);
        } else {
            return wrapped.getShort(field);
        }
    }
    
    @Override
    public int getInt(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Integer)newValues.get(field);
        } else {
            return wrapped.getInt(field);
        }
    }
    
    @Override
    public long getLong(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Long)newValues.get(field);
        } else {
            return wrapped.getLong(field);
        }
    }
    
    @Override
    public float getFloat(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Float)newValues.get(field);
        } else {
            return wrapped.getFloat(field);
        }
    }
    
    @Override
    public double getDouble(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Double)newValues.get(field);
        } else {
            return wrapped.getDouble(field);
        }
    }
    
    @Override
    public void set(FieldMirror field, ObjectMirror o) {
        newValues.put(field, o);
    }
    
    @Override
    public void setBoolean(FieldMirror field, boolean b) {
        newValues.put(field, b);
    }
    
    @Override
    public void setByte(FieldMirror field, byte b) {
        newValues.put(field, b);
    }
    
    @Override
    public void setChar(FieldMirror field, char c) {
        newValues.put(field, c);
    }
    
    @Override
    public void setShort(FieldMirror field, short s) {
        newValues.put(field, s);
    }
    
    @Override
    public void setInt(FieldMirror field, int i) {
        newValues.put(field, i);
    }
    
    @Override
    public void setLong(FieldMirror field, long l) {
        newValues.put(field, l);
    }
    
    @Override
    public void setFloat(FieldMirror field, float f) {
        newValues.put(field, f);
    }
    
    @Override
    public void setDouble(FieldMirror field, double d) {
        newValues.put(field, d);
    }

    
}
