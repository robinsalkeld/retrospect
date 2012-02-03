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
    
    
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
//        Class<?> c = klass;
//        Field field = null;
//        while (c != null) {
//            try {
//                field = c.getDeclaredField(name);
//                break;
//            } catch (NoSuchFieldException e) {
//            }
//            c = c.getSuperclass();
//        }
//        if (field == null) {
//            throw new NoSuchFieldException();
//        }
        return new MapEntryFieldMirror(null, name);
    }

    
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        throw new UnsupportedOperationException();
    }

    
    public ClassMirror<? extends T> getClassMirror() {
        return new ClassMirror<T>() {
            public String getClassName() {
                return klass.getName();
            }
            public FieldMirror getStaticField(String name) throws NoSuchFieldException {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    private class MapEntryFieldMirror implements FieldMirror {
        private final Field field;
        private final String name;

        public MapEntryFieldMirror(Field field, String name) {
            this.field = field;
            this.name = name;
        }
        
        public Class<?> getType() {
            return null;
        }

        public Object get() throws IllegalAccessException {
            return fields.get(name);
        }
        
        public boolean getBoolean() throws IllegalAccessException {
            Object value = get();
            return value == null ? false : ((Boolean)value).booleanValue();
        }

        
        public byte getByte() throws IllegalAccessException {
            Object value = get();
            return value == null ? 0 : ((Byte)value).byteValue();
        }

        
        public char getChar() throws IllegalAccessException {
            Object value = get();
            return value == null ? 0 : ((Character)value).charValue();
        }

        
        public short getShort() throws IllegalAccessException {
            Object value = get();
            return value == null ? 0 : ((Byte)value).byteValue();
        }

        
        public int getInt() throws IllegalAccessException {
            Object value = get();
            return value == null ? 0 : ((Integer)value).intValue();
        }

        
        public long getLong() throws IllegalAccessException {
            Object value = get();
            return value == null ? 0 : ((Long)value).longValue();
        }

        
        public float getFloat() throws IllegalAccessException {
            Object value = get();
            return value == null ? 0 : ((Float)value).floatValue();
        }

        
        public double getDouble() throws IllegalAccessException {
            Object value = get();
            return value == null ? 0 : ((Double)value).doubleValue();
        }

        public void set(Object o) throws IllegalAccessException {
            System.out.println("putting " + name + " => " + o);
            fields.put(name, o);
        }

        
        public void setBoolean(boolean b) throws IllegalAccessException {
            set(Boolean.valueOf(b));
        }

        
        public void setByte(byte b) throws IllegalAccessException {
            set(Byte.valueOf(b));
        }

        
        public void setChar(char c) throws IllegalAccessException {
            set(Character.valueOf(c));
        }

        
        public void setShort(short s) throws IllegalAccessException {
            set(Short.valueOf(s));
        }

        
        public void setInt(int i) throws IllegalAccessException {
            set(Integer.valueOf(i));
        }

        
        public void setLong(long l) throws IllegalAccessException {
            set(Long.valueOf(l));
        }

        
        public void setFloat(float f) throws IllegalAccessException {
            set(Float.valueOf(f));
        }

        
        public void setDouble(double d) throws IllegalAccessException {
            set(Double.valueOf(d));
        }
    }

    
    public FieldMirror getArrayElement(int index) throws ArrayIndexOutOfBoundsException {
        return null;
    }
}
