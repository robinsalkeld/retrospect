package edu.ubc.mirrors;

public interface MethodMirrorExitRequest extends MirrorEventRequest {

    public void addClassFilter(ClassMirror klass);
    public void setMethodFilter(MethodMirror method);
}
