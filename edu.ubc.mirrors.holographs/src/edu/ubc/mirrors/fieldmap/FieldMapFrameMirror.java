package edu.ubc.mirrors.fieldmap;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.MethodMirror;

public class FieldMapFrameMirror implements FrameMirror {

    public FieldMapFrameMirror(MethodMirror method, String fileName, int lineNumber) {
        this.method = method;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }

    private final MethodMirror method;
    private final String fileName;
    private final int lineNumber;
    
    @Override
    public ClassMirror declaringClass() {
        return method.getDeclaringClass();
    }

    @Override
    public String methodName() {
        return method.getName();
    }

    @Override
    public MethodMirror method() {
        return method;
    }

    @Override
    public String fileName() {
        return fileName;
    }

    @Override
    public int lineNumber() {
        return lineNumber;
    }

}
