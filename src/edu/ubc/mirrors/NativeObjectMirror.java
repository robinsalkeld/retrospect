package edu.ubc.mirrors;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;


public class NativeObjectMirror<T> implements ObjectMirror<T> {

    private final T object;
    
    public NativeObjectMirror(T object) {
        this.object = object;
    }
    
    public static <T> NativeObjectMirror<T> make(Class<T> nativeClass) {
        try {
            return new NativeObjectMirror<T>(nativeClass.newInstance());
        } catch (IllegalAccessException e) {
            InternalError error = new InternalError();
            error.initCause(e);
            throw error;
        } catch (InstantiationException e) {
            InternalError error = new InternalError();
            error.initCause(e);
            throw error;
        }
    }
    
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        return getField(name, false);
    }
    
    
    private FieldMirror getField(String name, boolean isStatic) throws NoSuchFieldException {
        Field field = object.getClass().getDeclaredField(name);
        if (Modifier.isStatic(field.getModifiers()) == isStatic) {
            // Crap, fall back to manual search
            field = findField(name, isStatic);
        }
        return new NativeFieldMirror(field, object);
    }
    
    private Field findField(String name, boolean isStatic) throws NoSuchFieldException {
        for (Field f : object.getClass().getDeclaredFields()) {
            if (f.getName().equals(name) && Modifier.isStatic(f.getModifiers()) == isStatic) {
                return f;
            }
        }
        throw new NoSuchFieldException(name);
    }
    
    
    public FieldMirror getArrayElement(int index) throws IllegalStateException, ArrayIndexOutOfBoundsException {
        return new NativeArrayElementFieldMirror(index);
    }
    
    public int getArrayLength() throws IllegalStateException {
        return Array.getLength(object);
    }
    
    
    public ClassMirror<?> getClassMirror() {
        return new ClassMirror<T>() {
            public String getClassName() {
                return object.getClass().getName();
            }
            public boolean isArray() {
                return false;
            }
            public ClassMirror<?> getComponentClassMirror() {
                return null;
            }
            public FieldMirror getStaticField(String name) throws NoSuchFieldException {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    static class NativeFieldMirror implements FieldMirror {
        
        private final Field field;
        private final Object object;
        
        public NativeFieldMirror(Field field, Object object) {
            this.field = field;
            field.setAccessible(true);
            this.object = object;
        }

        public Object get() throws IllegalAccessException {
            return field.get(object);
        }

        public boolean getBoolean() throws IllegalAccessException {
            return field.getBoolean(object);
        }

        public byte getByte() throws IllegalAccessException {
            return field.getByte(object);
        }

        public char getChar() throws IllegalAccessException {
            return field.getChar(object);
        }

        public short getShort() throws IllegalAccessException {
            return field.getShort(object);
        }

        public int getInt() throws IllegalAccessException {
            return field.getInt(object);
        }

        public long getLong() throws IllegalAccessException {
            return field.getLong(object);
        }

        public float getFloat() throws IllegalAccessException {
            return field.getFloat(object);
        }

        public double getDouble() throws IllegalAccessException {
            return field.getDouble(object);
        }

        public void set(Object o) throws IllegalAccessException {
            field.set(object, o);
        }

        public void setBoolean(boolean b) throws IllegalAccessException {
            field.set(object, b);
        }

        public void setByte(byte b) throws IllegalAccessException {
            field.set(object, b);
        }

        public void setChar(char c) throws IllegalAccessException {
            field.set(object, c);
        }

        public void setShort(short s) throws IllegalAccessException {
            field.set(object, s);
        }

        public void setInt(int i) throws IllegalAccessException {
            field.set(object, i);
        }

        public void setLong(long l) throws IllegalAccessException {
            field.set(object, l);
        }

        public void setFloat(float f) throws IllegalAccessException {
            field.set(object, f);
        }

        public void setDouble(double d) throws IllegalAccessException {
            field.set(object, d);
        }
    }
    
    private class NativeArrayElementFieldMirror implements FieldMirror {
        
        private final int index;
        
        public NativeArrayElementFieldMirror(int index) {
            this.index = index;
        }

        public Object get() throws IllegalAccessException {
            return Array.get(object, index);
        }

        
        public boolean getBoolean() throws IllegalAccessException {
            return Array.getBoolean(object, index);
        }

        
        public byte getByte() throws IllegalAccessException {
            return Array.getByte(object, index);
        }

        
        public char getChar() throws IllegalAccessException {
            return Array.getChar(object, index);
        }

        
        public short getShort() throws IllegalAccessException {
            return Array.getShort(object, index);
        }

        
        public int getInt() throws IllegalAccessException {
            return Array.getInt(object, index);
        }

        
        public long getLong() throws IllegalAccessException {
            return Array.getLong(object, index);
        }

        
        public float getFloat() throws IllegalAccessException {
            return Array.getFloat(object, index);
        }

        
        public double getDouble() throws IllegalAccessException {
            return Array.getDouble(object, index);
        }

        
        public void set(Object o) throws IllegalAccessException {
            Array.set(object, index, o);
        }

        
        public void setBoolean(boolean b) throws IllegalAccessException {
            Array.setBoolean(object, index, b);
        }

        
        public void setByte(byte b) throws IllegalAccessException {
            Array.setByte(object, index, b);
        }

        
        public void setChar(char c) throws IllegalAccessException {
            Array.setChar(object, index, c);
        }

        
        public void setShort(short s) throws IllegalAccessException {
            Array.setShort(object, index, s);
        }

        
        public void setInt(int i) throws IllegalAccessException {
            Array.setInt(object, index, i);
        }

        
        public void setLong(long l) throws IllegalAccessException {
            Array.setLong(object, index, l);
        }

        
        public void setFloat(float f) throws IllegalAccessException {
            Array.setFloat(object, index, f);
        }

        
        public void setDouble(double d) throws IllegalAccessException {
            Array.setDouble(object, index, d);
        }
        
    }
}
