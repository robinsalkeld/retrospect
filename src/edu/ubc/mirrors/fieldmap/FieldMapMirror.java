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
import edu.ubc.mirrors.raw.NativeInstanceMirror;

public class FieldMapMirror implements InstanceMirror {

    private final Map<String, Object> fields;
    private final ClassMirror classMirror;
    
    public FieldMapMirror(ClassMirror classMirror) {
        this.classMirror = classMirror;
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
        ClassMirror c = classMirror;
        while (c != null) {
            for (String name : c.getDeclaredFieldNames()) {
                try {
                    result.add(getMemberField(name));
                } catch (NoSuchFieldException e) {
                    throw new NoSuchFieldError(e.getMessage());
                }
            }
            c = c.getSuperClassMirror();
        }
        return result;
    }
    
    public ClassMirror getClassMirror() {
        return classMirror;
    }
    
    private class MapEntryFieldMirror extends BoxingFieldMirror {
        private final Field field;
        private final String name;

        public MapEntryFieldMirror(Field field, String name) {
            this.field = field;
            this.name = name;
        }
        
        private FieldMapMirror getEnclosingThis() {
            return FieldMapMirror.this;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MapEntryFieldMirror)) {
                return false;
            }
            
            MapEntryFieldMirror other = (MapEntryFieldMirror)obj;
            return getEnclosingThis().equals(other.getEnclosingThis())
                && name.equals(other.name);
        }
        
        @Override
        public int hashCode() {
            return getEnclosingThis().hashCode() + name.hashCode();
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public ClassMirror getType() {
            return (ClassMirror)NativeInstanceMirror.makeMirror(field.getType());
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
