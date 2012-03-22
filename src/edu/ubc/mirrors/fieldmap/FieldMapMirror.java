package edu.ubc.mirrors.fieldmap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ubc.mirrors.BoxingFieldMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;

public class FieldMapMirror implements InstanceMirror {

    private final Map<String, Object> fields;
    private final Class<?> klass;
    
    public FieldMapMirror(Class<?> klass) {
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

    @Override
    public List<FieldMirror> getMemberFields() {
        List<FieldMirror> result = new ArrayList<FieldMirror>();
        Class<?> c = klass;
        while (c != null) {
            for (Field f : c.getDeclaredFields()) {
                try {
                    result.add(getMemberField(f.getName()));
                } catch (NoSuchFieldException e) {
                    throw new NoSuchFieldError(e.getMessage());
                }
            }
            c = c.getSuperclass();
        }
        return result;
    }
    
    public ClassMirror getClassMirror() {
        return new NativeClassMirror(klass);
    }
    
    private class MapEntryFieldMirror extends BoxingFieldMirror {
        private final Field field;
        private final String name;

        public MapEntryFieldMirror(Field field, String name) {
            this.field = field;
            this.name = name;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public Class<?> getType() {
            return field.getType();
        }
        
        public ObjectMirror get() throws IllegalAccessException {
            return (ObjectMirror)getBoxedValue();
        }
        
        public void set(ObjectMirror o) throws IllegalAccessException {
            setBoxedValue(o);
        }
        
        public Object getBoxedValue() throws IllegalAccessException {
            Object result = fields.get(name);
//            System.out.println("getting " + FieldMapMirror.this.klass.getName() + "." + name + " => " + result);
            return result;
        }

        public void setBoxedValue(Object o) throws IllegalAccessException {
//            System.out.println("putting " + FieldMapMirror.this.klass.getName() + "." + name + " => " + o);
            fields.put(name, o);
        }
    }
}
