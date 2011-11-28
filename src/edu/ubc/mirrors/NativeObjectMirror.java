package edu.ubc.mirrors;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;


public class NativeObjectMirror<T> implements ObjectMirror<T> {

    private final T object;
    
    public NativeObjectMirror(T object) {
        this.object = object;
    }
    
    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        return getField(name, false);
    }
    
    @Override
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        return getField(name, true);
    }
    
    private FieldMirror getField(String name, boolean isStatic) throws NoSuchFieldException {
        Field field = object.getClass().getField(name);
        if (Modifier.isStatic(field.getModifiers()) == isStatic) {
            // Crap, fall back to manual search
            field = findField(name, isStatic);
        }
        return new NativeFieldMirror(field);
    }
    
    private Field findField(String name, boolean isStatic) throws NoSuchFieldException {
        for (Field f : object.getClass().getFields()) {
            if (f.getName().equals(name) && Modifier.isStatic(f.getModifiers()) == isStatic) {
                return f;
            }
        }
        throw new NoSuchFieldException(name);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends T> getClassMirror() {
        return (Class<? extends T>) object.getClass();
    }
    
    private class NativeFieldMirror implements FieldMirror {
        
        private final Field field;
        
        public NativeFieldMirror(Field field) {
            this.field = field;
        }

        @Override
        public Class<?> getType() {
            return field.getType();
        }
        
        @Override
        public Object get() throws IllegalAccessException {
            return field.get(object);
        }

        @Override
        public boolean getBoolean() throws IllegalAccessException {
            return field.getBoolean(object);
        }

        @Override
        public byte getByte() throws IllegalAccessException {
            return field.getByte(object);
        }

        @Override
        public char getChar() throws IllegalAccessException {
            return field.getChar(object);
        }

        @Override
        public short getShort() throws IllegalAccessException {
            return field.getShort(object);
        }

        @Override
        public int getInt() throws IllegalAccessException {
            return field.getInt(object);
        }

        @Override
        public long getLong() throws IllegalAccessException {
            return field.getLong(object);
        }

        @Override
        public float getFloat() throws IllegalAccessException {
            return field.getFloat(object);
        }

        @Override
        public double getDouble() throws IllegalAccessException {
            return field.getDouble(object);
        }

        @Override
        public void set(Object o) throws IllegalAccessException {
            field.set(object, o);
        }

        @Override
        public void setBoolean(boolean b) throws IllegalAccessException {
            field.set(object, b);
        }

        @Override
        public void setByte(byte b) throws IllegalAccessException {
            field.set(object, b);
        }

        @Override
        public void setChar(char c) throws IllegalAccessException {
            field.set(object, c);
        }

        @Override
        public void setShort(short s) throws IllegalAccessException {
            field.set(object, s);
        }

        @Override
        public void setInt(int i) throws IllegalAccessException {
            field.set(object, i);
        }

        @Override
        public void setLong(long l) throws IllegalAccessException {
            field.set(object, l);
        }

        @Override
        public void setFloat(float f) throws IllegalAccessException {
            field.set(object, f);
        }

        @Override
        public void setDouble(double d) throws IllegalAccessException {
            field.set(object, d);
        }
    }
}
