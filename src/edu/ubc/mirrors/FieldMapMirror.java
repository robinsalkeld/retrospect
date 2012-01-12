package edu.ubc.mirrors;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class FieldMapMirror<T> implements ObjectMirror<T> {

    private final Map<String, Object> fields;
    private final Class<? extends T> klass;
    
    public FieldMapMirror(Class<? extends T> klass) {
        this.klass = klass;
        this.fields = new HashMap<String, Object>();
    }
    
    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        Field field = klass.getDeclaredField(name);
        return new MapEntryFieldMirror(field);
    }

    @Override
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<? extends T> getClassMirror() {
        return klass;
    }
    
    private class MapEntryFieldMirror implements FieldMirror {
        private final Field field;

        public MapEntryFieldMirror(Field field) {
            this.field = field;
        }
        
        @Override
        public Class<?> getType() {
            return field.getType();
        }

        @Override
        public Object get() throws IllegalAccessException {
            return fields.get(field.getName());
        }

        @Override
        public boolean getBoolean() throws IllegalAccessException {
            Object value = get();
            return value == null ? false : ((Boolean)value).booleanValue();
        }

        @Override
        public byte getByte() throws IllegalAccessException {
            Object value = get();
            return value == null ? 0 : ((Byte)value).byteValue();
        }

        @Override
        public char getChar() throws IllegalAccessException {
            Object value = get();
            return value == null ? 0 : ((Character)value).charValue();
        }

        @Override
        public short getShort() throws IllegalAccessException {
            Object value = get();
            return value == null ? 0 : ((Byte)value).byteValue();
        }

        @Override
        public int getInt() throws IllegalAccessException {
            Object value = get();
            return value == null ? 0 : ((Integer)value).intValue();
        }

        @Override
        public long getLong() throws IllegalAccessException {
            Object value = get();
            return value == null ? 0 : ((Long)value).longValue();
        }

        @Override
        public float getFloat() throws IllegalAccessException {
            Object value = get();
            return value == null ? 0 : ((Float)value).floatValue();
        }

        @Override
        public double getDouble() throws IllegalAccessException {
            Object value = get();
            return value == null ? 0 : ((Double)value).doubleValue();
        }

        @Override
        public void set(Object o) throws IllegalAccessException {
            fields.put(field.getName(), o);
        }

        @Override
        public void setBoolean(boolean b) throws IllegalAccessException {
            set(Boolean.valueOf(b));
        }

        @Override
        public void setByte(byte b) throws IllegalAccessException {
            set(Byte.valueOf(b));
        }

        @Override
        public void setChar(char c) throws IllegalAccessException {
            set(Character.valueOf(c));
        }

        @Override
        public void setShort(short s) throws IllegalAccessException {
            set(Short.valueOf(s));
        }

        @Override
        public void setInt(int i) throws IllegalAccessException {
            set(Integer.valueOf(i));
        }

        @Override
        public void setLong(long l) throws IllegalAccessException {
            set(Long.valueOf(l));
        }

        @Override
        public void setFloat(float f) throws IllegalAccessException {
            set(Float.valueOf(f));
        }

        @Override
        public void setDouble(double d) throws IllegalAccessException {
            set(Double.valueOf(d));
        }
    }

    @Override
    public FieldMirror getArrayElement(int index) throws ArrayIndexOutOfBoundsException {
        return null;
    }
}
