package edu.ubc.mirrors;

public interface ConstructorMirrorEntryRequest extends MirrorEventRequest {

    public void addClassFilter(ClassMirror klass);
    public void setConstructorFilter(ConstructorMirror method);
}
