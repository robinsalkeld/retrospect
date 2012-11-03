package edu.ubc.mirrors.fieldmap;

import edu.ubc.mirrors.BoxingInstanceMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;

public class FieldMapMirror extends BoxingInstanceMirror {

    protected final ClassMirror classMirror;
    
    public FieldMapMirror(ClassMirror classMirror) {
        this.classMirror = classMirror;
    }

    public ClassMirror getClassMirror() {
        return classMirror;
    }
    
    @Override
    public Object getBoxedValue(FieldMirror field) throws IllegalAccessException {
        return null;
    }
    
    @Override
    public void setBoxedValue(FieldMirror field, Object o) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }
}
