package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.ObjectMirror;

public class ClassClassMirror extends NativeClassMirror {

    public ClassClassMirror(Class<?> klass) {
        super(klass);
    }
    
    public ObjectMirror getName0() {
        return new NativeObjectMirror(getClassName());
    }
    
}
