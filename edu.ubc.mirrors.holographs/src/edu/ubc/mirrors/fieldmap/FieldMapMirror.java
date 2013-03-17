package edu.ubc.mirrors.fieldmap;

import edu.ubc.mirrors.BlankInstanceMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.holographs.NewInstanceMirror;

public class FieldMapMirror extends BlankInstanceMirror implements NewInstanceMirror {

    protected final ClassMirror classMirror;
    
    public FieldMapMirror(ClassMirror classMirror) {
        this.classMirror = classMirror;
    }

    public ClassMirror getClassMirror() {
        return classMirror;
    }
    
    @Override
    public int identityHashCode() {
        return hashCode();
    }
}
