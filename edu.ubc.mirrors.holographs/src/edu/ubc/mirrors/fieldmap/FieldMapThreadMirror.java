package edu.ubc.mirrors.fieldmap;

import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.ThreadMirror;

public class FieldMapThreadMirror extends FieldMapMirror implements ThreadMirror {

    public FieldMapThreadMirror(ClassMirror classMirror) {
        super(classMirror);
    }

    @Override
    public List<FrameMirror> getStackTrace() {
        return null;
    }
    
    @Override
    public void interrupt() {
        throw new UnsupportedOperationException(); 
    }
}
