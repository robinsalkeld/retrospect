package edu.ubc.mirrors.fieldmap;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;

public class FieldMapFieldMirror implements FieldMirror {

    private final ClassMirror klass;
    private final String name;
    protected ClassMirror type;
    private final int access;
    
    public FieldMapFieldMirror(ClassMirror klass, int access, String name, ClassMirror type) {
        this.klass = klass;
        this.access = access;
        this.name = name;
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        
        FieldMapFieldMirror other = (FieldMapFieldMirror)obj;
        return klass.equals(other.klass) && name.equals(other.name);
    }
    
    @Override
    public int hashCode() {
        return 17 + klass.hashCode() + name.hashCode();
    }
    
    @Override
    public ClassMirror getDeclaringClass() {
        return klass;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTypeName() {
        return type.getClassName();
    }
    
    @Override
    public ClassMirror getType() {
        return type;
    }

    @Override
    public int getModifiers() {
        return access;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + type + " " + name;
    }
}
