package edu.ubc.mirrors;

public interface ConstructorMirrorExitRequest extends MirrorEventRequest {

    public void addClassFilter(ClassMirror klass);
    public void setConstructorFilter(ConstructorMirror method);
}
