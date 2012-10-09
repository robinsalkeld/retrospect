package edu.ubc.mirrors;

public interface FrameMirror {

    ClassMirror declaringClass();
    String methodName();
    String fileName();
    int lineNumber();
    
}
