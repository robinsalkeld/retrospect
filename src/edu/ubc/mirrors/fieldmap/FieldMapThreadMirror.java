package edu.ubc.mirrors.fieldmap;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ThreadMirror;

public class FieldMapThreadMirror extends FieldMapMirror implements ThreadMirror {

    public FieldMapThreadMirror(ClassMirror classMirror) {
        super(classMirror);
    }

    @Override
    public ObjectArrayMirror getStackTrace() {
        return null;
    }
}
