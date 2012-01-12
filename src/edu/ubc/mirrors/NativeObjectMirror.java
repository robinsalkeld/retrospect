package edu.ubc.mirrors;

import java.lang.reflect.Array;
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
        Field field = object.getClass().getDeclaredField(name);
        if (Modifier.isStatic(field.getModifiers()) == isStatic) {
            // Crap, fall back to manual search
            field = findField(name, isStatic);
        }
        return new NativeFieldMirror(field);
    }
    
    private Field findField(String name, boolean isStatic) throws NoSuchFieldException {
        for (Field f : object.getClass().getDeclaredFields()) {
            if (f.getName().equals(name) && Modifier.isStatic(f.getModifiers()) == isStatic) {
                return f;
            }
        }
        throw new NoSuchFieldException(name);
    }
    
    @Override
    public FieldMirror getArrayElement(int index)
            throws ArrayIndexOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
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
            field.setAccessible(true);
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
    
    private class NativeArrayElementFieldMirror implements FieldMirror {
        
        private final int index;
        
        public NativeArrayElementFieldMirror(int index) {
            this.index = index;
        }

        @Override
        public Class<?> getType() {
            return object.getClass().getComponentType();
        }

        @Override
        public Object get() throws IllegalAccessException {
            return Array.get(object, index);
        }

        @Override
        public boolean getBoolean() throws IllegalAccessException {
            return Array.getBoolean(object, index);
        }

        @Override
        public byte getByte() throws IllegalAccessException {
            return Array.getByte(object, index);
        }

        @Override
        public char getChar() throws IllegalAccessException {
            return Array.getChar(object, index);
        }

        @Override
        public short getShort() throws IllegalAccessException {
            return Array.getShort(object, index);
        }

        @Override
        public int getInt() throws IllegalAccessException {
            return Array.getInt(object, index);
        }

        @Override
        public long getLong() throws IllegalAccessException {
            return Array.getLong(object, index);
        }

        @Override
        public float getFloat() throws IllegalAccessException {
            return Array.getFloat(object, index);
        }

        @Override
        public double getDouble() throws IllegalAccessException {
            return Array.getDouble(object, index);
        }

        @Override
        public void set(Object o) throws IllegalAccessException {
            Array.set(object, index, o);
        }

        @Override
        public void setBoolean(boolean b) throws IllegalAccessException {
            Array.setBoolean(object, index, b);
        }

        @Override
        public void setByte(byte b) throws IllegalAccessException {
            Array.setByte(object, index, b);
        }

        @Override
        public void setChar(char c) throws IllegalAccessException {
            Array.setChar(object, index, c);
        }

        @Override
        public void setShort(short s) throws IllegalAccessException {
            Array.setShort(object, index, s);
        }

        @Override
        public void setInt(int i) throws IllegalAccessException {
            Array.setInt(object, index, i);
        }

        @Override
        public void setLong(long l) throws IllegalAccessException {
            Array.setLong(object, index, l);
        }

        @Override
        public void setFloat(float f) throws IllegalAccessException {
            Array.setFloat(object, index, f);
        }

        @Override
        public void setDouble(double d) throws IllegalAccessException {
            Array.setDouble(object, index, d);
        }
        
    }
}
