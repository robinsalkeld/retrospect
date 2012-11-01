package edu.ubc.mirrors.raw;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;


public class NativeInstanceMirror extends NativeObjectMirror implements InstanceMirror {

    private final Object object;
    
    public NativeInstanceMirror(Object object) {
        super(object);
        this.object = object;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NativeInstanceMirror)) {
            return false;
        }
        
        return object == ((NativeInstanceMirror)obj).object;
    }
    
    @Override
    public int hashCode() {
        return 17 + System.identityHashCode(object);
    }
    
    public static NativeInstanceMirror make(Class<?> nativeClass) {
        try {
            return new NativeInstanceMirror(nativeClass.newInstance());
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
    
    public ClassMirror getClassMirror() {
        return new NativeClassMirror(object.getClass());
    }
    
    public static class NativeFieldMirror implements FieldMirror {
        
        private final Field field;
        
        public NativeFieldMirror(Field field) {
            this.field = field;
            field.setAccessible(true);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof NativeFieldMirror)) {
                return false;
            }
            
            NativeFieldMirror other = (NativeFieldMirror)obj;
            return field.equals(other.field);
        }
        
        @Override
        public int hashCode() {
            return field.hashCode();
        }
        
        @Override
        public ClassMirror getDeclaringClass() {
            return (ClassMirror)NativeInstanceMirror.makeMirror(field.getDeclaringClass());
        }
        
        @Override
        public String getName() {
            return field.getName();
        }
        
        @Override
        public ClassMirror getType() {
            return (ClassMirror)NativeInstanceMirror.makeMirror(field.getType());
        }
        
        @Override
        public int getModifiers() {
            return field.getModifiers();
        }
        
        private Object getNativeObject(ObjectMirror obj) {
            return ((NativeObjectMirror)obj).object;
        }
        
        public ObjectMirror get(InstanceMirror obj) throws IllegalAccessException {
            Object nativeValue = field.get(getNativeObject(obj));
            return makeMirror(nativeValue);
        }

        public boolean getBoolean(InstanceMirror obj) throws IllegalAccessException {
            return field.getBoolean(getNativeObject(obj));
        }

        public byte getByte(InstanceMirror obj) throws IllegalAccessException {
            return field.getByte(getNativeObject(obj));
        }

        public char getChar(InstanceMirror obj) throws IllegalAccessException {
            return field.getChar(getNativeObject(obj));
        }

        public short getShort(InstanceMirror obj) throws IllegalAccessException {
            return field.getShort(getNativeObject(obj));
        }

        public int getInt(InstanceMirror obj) throws IllegalAccessException {
            return field.getInt(getNativeObject(obj));
        }

        public long getLong(InstanceMirror obj) throws IllegalAccessException {
            return field.getLong(getNativeObject(obj));
        }

        public float getFloat(InstanceMirror obj) throws IllegalAccessException {
            return field.getFloat(getNativeObject(obj));
        }

        public double getDouble(InstanceMirror obj) throws IllegalAccessException {
            return field.getDouble(getNativeObject(obj));
        }

        public void set(InstanceMirror obj, ObjectMirror o) throws IllegalAccessException {
            field.set(getNativeObject(obj), getNativeObject(o));
        }

        public void setBoolean(InstanceMirror obj, boolean b) throws IllegalAccessException {
            field.setBoolean(getNativeObject(obj), b);
        }

        public void setByte(InstanceMirror obj, byte b) throws IllegalAccessException {
            field.setByte(getNativeObject(obj), b);
        }

        public void setChar(InstanceMirror obj, char c) throws IllegalAccessException {
            field.setChar(getNativeObject(obj), c);
        }

        public void setShort(InstanceMirror obj, short s) throws IllegalAccessException {
            field.setShort(getNativeObject(obj), s);
        }

        public void setInt(InstanceMirror obj, int i) throws IllegalAccessException {
            field.setInt(getNativeObject(obj), i);
        }

        public void setLong(InstanceMirror obj, long l) throws IllegalAccessException {
            field.setLong(getNativeObject(obj), l);
        }

        public void setFloat(InstanceMirror obj, float f) throws IllegalAccessException {
            field.setFloat(getNativeObject(obj), f);
        }

        public void setDouble(InstanceMirror obj, double d) throws IllegalAccessException {
            field.setDouble(getNativeObject(obj), d);
        }
    }
    
    public static ObjectMirror makeMirror(Object object) {
        if (object == null) {
            return null;
        }
        
        if (object instanceof Class) {
            return new NativeClassMirror((Class<?>)object);
        } else if (object instanceof ClassLoader) {
            return new NativeClassMirrorLoader((ClassLoader)object);
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
            return new NativeInstanceMirror(object);
        }
    }
    
    @Override
    public String toString() {
        return "NativeObjectMirror: " + object;
    }
}
