package edu.ubc.mirrors.fieldmap;

import edu.ubc.mirrors.BoxingFieldMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpClassMirror;

public class ClassFieldMirror extends BoxingFieldMirror {
    private final ClassMirror klass;
    private final String name;
    
    public ClassFieldMirror(ClassMirror klass, String name) {
        this.klass = klass;
        this.name = name;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ClassFieldMirror)) {
            return false;
        }
        
        ClassFieldMirror other = (ClassFieldMirror)obj;
        return klass.equals(other.klass) && name.equals(other.name);
    }
    
    @Override
    public int hashCode() {
        return klass.hashCode() * 32 + name.hashCode();
    }
    
    @Override
    public String getName() {
        return name;
    }
    @Override
    public ClassMirror getType() {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public ObjectMirror get() throws IllegalAccessException {
        return null;
    }
    @Override
    public Object getBoxedValue() throws IllegalAccessException {
        return null;
    }
    @Override
    public void set(ObjectMirror o) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }
    @Override
    public void setBoxedValue(Object o) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }
}