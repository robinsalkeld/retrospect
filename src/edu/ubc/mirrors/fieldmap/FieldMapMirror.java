package edu.ubc.mirrors.fieldmap;

import java.lang.reflect.Field;
import java.util.HashMap;
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
        if (name.equals("scopeStack")) {
            int bp = 5;
        }
        return new MapEntryFieldMirror(null, name);
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
