package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.StaticFieldValuesMirror;

public class MutableStaticFieldValuesMirror extends MutableInstanceMirror implements StaticFieldValuesMirror {

    private StaticFieldValuesMirror wrapped;
    
    public MutableStaticFieldValuesMirror(StaticFieldValuesMirror wrapped) {
        super(wrapped);
        this.wrapped = wrapped;
    }
    
    @Override
    public ClassMirror forClassMirror() {
        return wrapped.forClassMirror();
    }
}
