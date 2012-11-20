package edu.ubc.mirrors.fieldmap;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;

public class FieldMapClassMirrorLoader extends FieldMapMirror implements ClassMirrorLoader {

    public FieldMapClassMirrorLoader(ClassMirror classMirror) {
        super(classMirror);
    }

    @Override
    public ClassMirror findLoadedClassMirror(String name) {
        return null;
    }

    @Override
    public ClassMirror defineClass1(String name, ByteArrayMirror b, int off, int len, InstanceMirror pd, InstanceMirror source) {
        throw new UnsupportedOperationException();
    }
}
