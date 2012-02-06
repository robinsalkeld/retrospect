package edu.ubc.mirrors;

public interface ObjectMirror<T> {
    
    public ClassMirror<?> getClassMirror();
}
