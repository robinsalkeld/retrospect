package edu.ubc.mirrors.fieldmap;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;

public class FieldMapClassMirrorLoader extends FieldMapMirror implements ClassMirrorLoader {

    public FieldMapClassMirrorLoader(ClassMirror classMirror) {
        super(classMirror);
    }

    @Override
    public ClassMirror findLoadedClassMirror(String name) {
        return null;
    }

}
