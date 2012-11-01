package edu.ubc.mirrors;

public abstract class BoxingFieldMirror implements FieldMirror {

    public abstract Object getBoxedValue(InstanceMirror obj) throws IllegalAccessException;
    
    public boolean getBoolean(InstanceMirror obj) throws IllegalAccessException {
        Object value = getBoxedValue(obj);
        return value == null ? false : ((Boolean)value).booleanValue();
    }

    public byte getByte(InstanceMirror obj) throws IllegalAccessException {
        Object value = getBoxedValue(obj);
        return value == null ? 0 : ((Byte)value).byteValue();
    }
    
    public char getChar(InstanceMirror obj) throws IllegalAccessException {
        Object value = getBoxedValue(obj);
        return value == null ? 0 : ((Character)value).charValue();
    }

    public short getShort(InstanceMirror obj) throws IllegalAccessException {
        Object value = getBoxedValue(obj);
        return value == null ? 0 : ((Short)value).shortValue();
    }
    
    public int getInt(InstanceMirror obj) throws IllegalAccessException {
        Object value = getBoxedValue(obj);
        return value == null ? 0 : ((Integer)value).intValue();
    }
    
    public long getLong(InstanceMirror obj) throws IllegalAccessException {
        Object value = getBoxedValue(obj);
        return value == null ? 0 : ((Long)value).longValue();
    }

    public float getFloat(InstanceMirror obj) throws IllegalAccessException {
        Object value = getBoxedValue(obj);
        return value == null ? 0 : ((Float)value).floatValue();
    }

    public double getDouble(InstanceMirror obj) throws IllegalAccessException {
        Object value = getBoxedValue(obj);
        return value == null ? 0 : ((Double)value).doubleValue();
    }

    public abstract void setBoxedValue(InstanceMirror obj, Object o) throws IllegalAccessException;

    public void setBoolean(InstanceMirror obj, boolean b) throws IllegalAccessException {
        setBoxedValue(obj, Boolean.valueOf(b));
    }

    public void setByte(InstanceMirror obj, byte b) throws IllegalAccessException {
        setBoxedValue(obj, Byte.valueOf(b));
    }

    public void setChar(InstanceMirror obj, char c) throws IllegalAccessException {
        setBoxedValue(obj, Character.valueOf(c));
    }

    public void setShort(InstanceMirror obj, short s) throws IllegalAccessException {
        setBoxedValue(obj, Short.valueOf(s));
    }
    
    public void setInt(InstanceMirror obj, int i) throws IllegalAccessException {
        setBoxedValue(obj, Integer.valueOf(i));
    }

    public void setLong(InstanceMirror obj, long l) throws IllegalAccessException {
        setBoxedValue(obj, Long.valueOf(l));
    }
    
    public void setFloat(InstanceMirror obj, float f) throws IllegalAccessException {
        setBoxedValue(obj, Float.valueOf(f));
    }
    
    public void setDouble(InstanceMirror obj, double d) throws IllegalAccessException {
        setBoxedValue(obj, Double.valueOf(d));
    }
}
