package edu.ubc.mirrors.fieldmap;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.NewHolographInstance;
import edu.ubc.mirrors.mirages.Reflection;

public class FieldMapMirror implements InstanceMirror, NewHolographInstance {

    protected final ClassMirror classMirror;
    
    public FieldMapMirror(ClassMirror classMirror) {
        this.classMirror = classMirror;
    }

    public ClassMirror getClassMirror() {
        return classMirror;
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
            for (FieldMirror field : Reflection.getAllFields(classMirror)) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(field.getName());
                sb.append("=");
                Object value;
                try {
                    value = Reflection.getBoxedValue(field, this);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
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
