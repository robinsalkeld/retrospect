package edu.ubc.mirrors.fieldmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ubc.mirrors.BoxingFieldMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;

public class FieldMapMirror implements InstanceMirror {

    private final Map<String, Object> fields;
    private final ClassMirror classMirror;
    
    public FieldMapMirror(ClassMirror classMirror) {
        this.classMirror = classMirror;
        this.fields = new HashMap<String, Object>();
    }
    
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        ClassMirror c = classMirror;
        ClassMirror fieldType = null;
        while (c != null) {
            fieldType = c.getDeclaredFields().get(name);
            if (fieldType != null) {
                break;
            }
            c = c.getSuperClassMirror();
        }
        if (fieldType == null) {
            throw new NoSuchFieldException();
        }
        return new MapEntryFieldMirror(name, fieldType);
    }

    @Override
    public List<FieldMirror> getMemberFields() {
        List<FieldMirror> result = new ArrayList<FieldMirror>();
        ClassMirror c = classMirror;
        while (c != null) {
            for (Map.Entry<String, ClassMirror> entry : c.getDeclaredFields().entrySet()) {
                try {
                    result.add(getMemberField(entry.getKey()));
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
    
    protected class MapEntryFieldMirror extends BoxingFieldMirror {
        private final ClassMirror type;
        private final String name;

        public MapEntryFieldMirror(String name, ClassMirror type) {
            this.type = type;
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
            return type;
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
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb, 3);
        return sb.toString();
    }
    
    public void toString(StringBuilder sb, int maxDepth) {
        sb.append(getClass().getSimpleName() + "[" + classMirror.getClassName() + "]@" + System.identityHashCode(this) + "(");
        if (maxDepth == 0) {
            sb.append("...");
        } else {
            boolean first = true;
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(entry.getKey());
                sb.append("=");
                Object value = entry.getValue();
                if (value instanceof FieldMapMirror) {
                    ((FieldMapMirror)value).toString(sb, maxDepth - 1);
                } else {
                    sb.append(value);
                }
            }
        }
        sb.append(")");
    }
}
