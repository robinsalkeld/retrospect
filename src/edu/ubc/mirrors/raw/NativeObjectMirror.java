package edu.ubc.mirrors.raw;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.jhat.HeapDumpObjectArrayMirror;
import edu.ubc.mirrors.jhat.HeapDumpObjectMirror;
import edu.ubc.mirrors.jhat.HeapDumpPrimitiveArrayMirror;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;


public class NativeObjectMirror implements InstanceMirror {

    private final Object object;
    
    public NativeObjectMirror(Object object) {
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
        if (object instanceof Object[]) {
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
