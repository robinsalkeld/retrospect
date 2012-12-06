package edu.ubc.mirrors;

public interface FrameMirror {

    ClassMirror declaringClass();
    String methodName();
    MethodMirror method();
    String fileName();
    int lineNumber();
    
}
