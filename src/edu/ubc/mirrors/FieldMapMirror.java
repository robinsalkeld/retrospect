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
    
    private class MapEntryFieldMirror extends BoxingFieldMirror {
        private final Field field;
        private final String name;

        public MapEntryFieldMirror(Field field, String name) {
            this.field = field;
            this.name = name;
        }
        
        public Object get() throws IllegalAccessException {
            return fields.get(name);
        }

        public void set(Object o) throws IllegalAccessException {
            System.out.println("putting " + name + " => " + o);
            fields.put(name, o);
        }
    }

    
    public FieldMirror getArrayElement(int index) throws ArrayIndexOutOfBoundsException {
        throw new IllegalStateException();
    }
    public int getArrayLength() throws IllegalStateException {
        throw new IllegalStateException();
    }
}
