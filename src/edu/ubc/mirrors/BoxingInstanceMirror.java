package edu.ubc.mirrors;

public abstract class BoxingInstanceMirror implements InstanceMirror {

    public abstract Object getBoxedValue(FieldMirror field) throws IllegalAccessException;
    
    @Override
    public ObjectMirror get(FieldMirror field) throws IllegalAccessException {
        return (ObjectMirror)getBoxedValue(field);
    }
    
    public boolean getBoolean(FieldMirror field) throws IllegalAccessException {
        Object value = getBoxedValue(field);
        return value == null ? false : ((Boolean)value).booleanValue();
    }

    public byte getByte(FieldMirror field) throws IllegalAccessException {
        Object value = getBoxedValue(field);
        return value == null ? 0 : ((Byte)value).byteValue();
    }
    
    public char getChar(FieldMirror field) throws IllegalAccessException {
        Object value = getBoxedValue(field);
        return value == null ? 0 : ((Character)value).charValue();
    }

    public short getShort(FieldMirror field) throws IllegalAccessException {
        Object value = getBoxedValue(field);
        return value == null ? 0 : ((Short)value).shortValue();
    }
    
    public int getInt(FieldMirror field) throws IllegalAccessException {
        Object value = getBoxedValue(field);
        return value == null ? 0 : ((Integer)value).intValue();
    }
    
    public long getLong(FieldMirror field) throws IllegalAccessException {
        Object value = getBoxedValue(field);
        return value == null ? 0 : ((Long)value).longValue();
    }

    public float getFloat(FieldMirror field) throws IllegalAccessException {
        Object value = getBoxedValue(field);
        return value == null ? 0 : ((Float)value).floatValue();
    }

    public double getDouble(FieldMirror field) throws IllegalAccessException {
        Object value = getBoxedValue(field);
        return value == null ? 0 : ((Double)value).doubleValue();
    }

    public abstract void setBoxedValue(FieldMirror field, Object o) throws IllegalAccessException;

    public void set(FieldMirror field, ObjectMirror o) throws IllegalAccessException {
         setBoxedValue(field, o);
    }
    
    public void setBoolean(FieldMirror field, boolean b) throws IllegalAccessException {
        setBoxedValue(field, Boolean.valueOf(b));
    }

    public void setByte(FieldMirror field, byte b) throws IllegalAccessException {
        setBoxedValue(field, Byte.valueOf(b));
    }

    public void setChar(FieldMirror field, char c) throws IllegalAccessException {
        setBoxedValue(field, Character.valueOf(c));
    }

    public void setShort(FieldMirror field, short s) throws IllegalAccessException {
        setBoxedValue(field, Short.valueOf(s));
    }
    
    public void setInt(FieldMirror field, int i) throws IllegalAccessException {
        setBoxedValue(field, Integer.valueOf(i));
    }

    public void setLong(FieldMirror field, long l) throws IllegalAccessException {
        setBoxedValue(field, Long.valueOf(l));
    }
    
    public void setFloat(FieldMirror field, float f) throws IllegalAccessException {
        setBoxedValue(field, Float.valueOf(f));
    }
    
    public void setDouble(FieldMirror field, double d) throws IllegalAccessException {
        setBoxedValue(field, Double.valueOf(d));
    }
}
