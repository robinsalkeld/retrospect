package edu.ubc.mirrors;

public interface MethodMirrorEntryRequest extends MirrorEventRequest {

    public void addClassFilter(ClassMirror klass);
    public void setMethodFilter(MethodMirror method);
}
