package edu.ubc.mirrors.mutable;

import java.util.HashMap;
import java.util.Map;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;

public class MutableThreadMirror extends MutableInstanceMirror implements ThreadMirror {

    private final ThreadMirror immutableThreadMirror;
    
    public MutableThreadMirror(MutableClassMirrorLoader loader, ThreadMirror immutableThreadMirror) {
        super(loader, immutableThreadMirror);
        this.immutableThreadMirror = immutableThreadMirror;
    }

    @Override
    public ObjectArrayMirror getStackTrace() {
        return (ObjectArrayMirror)loader.makeMirror(immutableThreadMirror.getStackTrace());
    }

    
}
