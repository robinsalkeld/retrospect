package edu.ubc.mirrors.raw;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;


public class NativeObjectMirror implements InstanceMirror {

    private final Object object;
    
    public NativeObjectMirror(Object object) {
        if (object == null) {
            throw new NullPointerException();
        }
        this.object = object;
    }
    
    public static NativeObjectMirror make(Class<?> nativeClass) {
        try {
            return new NativeObjectMirror(nativeClass.newInstance());
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
        return getField(object, name, false);
    }
    
    public static List<FieldMirror> getMemberFields(Object object) {
        // TODO-RS: Cache this!
        // TODO-RS: And then possibly use it to implement an
        // alternate field access API based on offsets instead of names
        List<FieldMirror> result = new ArrayList<FieldMirror>();
        Class<?> c = object.getClass();
        while (c != null) {
            for (Field f : c.getDeclaredFields()) {
                result.add(new NativeFieldMirror(f, object));
            }
            c = c.getSuperclass();
        }
        return result;
    }
    
    @Override
    public List<FieldMirror> getMemberFields() {
        return getMemberFields(object);
    }
    
    public static FieldMirror getField(Object object, String name, boolean isStatic) throws NoSuchFieldException {
        Class<?> klass = object.getClass();
        while (klass != null) {
            Field field = findField(klass, name, isStatic);
            if (field != null) {
                return new NativeFieldMirror(field, object);
            }
            klass = klass.getSuperclass();
        }
        
        throw new NoSuchFieldException(name);
    }
    
    private static Field findField(Class<?> klass, String name, boolean isStatic) {
        try {
            Field field = klass.getDeclaredField(name);
            if (Modifier.isStatic(field.getModifiers()) == isStatic) {
                return field;
            }
        } catch (NoSuchFieldException e) {
            return null;
        }
        
        // Crap, fall back to manual search
        for (Field f : klass.getDeclaredFields()) {
            if (f.getName().equals(name) && Modifier.isStatic(f.getModifiers()) == isStatic) {
                return f;
            }
        }
        
        return null;
    }
    
    public ClassMirror getClassMirror() {
        return new NativeClassMirror(object.getClass());
    }
    
    static class NativeFieldMirror implements FieldMirror {
        
        private final Field field;
        private final Object object;
        
        public NativeFieldMirror(Field field, Object object) {
            this.field = field;
            field.setAccessible(true);
            this.object = object;
        }

        @Override
        public String getName() {
            return field.getName();
        }
        
        @Override
        public Class<?> getType() {
            return field.getType();
        }
        
        public ObjectMirror get() throws IllegalAccessException {
            Object nativeValue = field.get(object);
            return makeMirror(nativeValue);
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

        public void set(ObjectMirror o) throws IllegalAccessException {
            throw new UnsupportedOperationException();
        }

        public void setBoolean(boolean b) throws IllegalAccessException {
            field.setBoolean(object, b);
        }

        public void setByte(byte b) throws IllegalAccessException {
            field.setByte(object, b);
        }

        public void setChar(char c) throws IllegalAccessException {
            field.setChar(object, c);
        }

        public void setShort(short s) throws IllegalAccessException {
            field.setShort(object, s);
        }

        public void setInt(int i) throws IllegalAccessException {
            field.setInt(object, i);
        }

        public void setLong(long l) throws IllegalAccessException {
            field.setLong(object, l);
        }

        public void setFloat(float f) throws IllegalAccessException {
            field.setFloat(object, f);
        }

        public void setDouble(double d) throws IllegalAccessException {
            field.setDouble(object, d);
        }
    }
    
    public static ObjectMirror makeMirror(Object object) {
        if (object == null) {
            return null;
        }
        
        if (object instanceof Class) {
            return new NativeClassMirror((Class<?>)object);
        } else if (object instanceof Thread) {
            return new NativeThreadMirror((Thread)object);
        } else if (object instanceof Object[]) {
            return new NativeObjectArrayMirror((Object[])object);
        } else if (object instanceof boolean[]) {
            return new NativeBooleanArrayMirror((boolean[])object);
        } else if (object instanceof byte[]) {
            return new NativeByteArrayMirror((byte[])object);
        } else if (object instanceof char[]) {
            return new NativeCharArrayMirror((char[])object);
        } else if (object instanceof short[]) {
            return new NativeShortArrayMirror((short[])object);
        } else if (object instanceof int[]) {
            return new NativeIntArrayMirror((int[])object);
        } else if (object instanceof long[]) {
            return new NativeLongArrayMirror((long[])object);
        } else if (object instanceof float[]) {
            return new NativeFloatArrayMirror((float[])object);
        } else if (object instanceof double[]) {
            return new NativeDoubleArrayMirror((double[])object);
        } else {
            return new NativeObjectMirror(object);
        }
    }
}
