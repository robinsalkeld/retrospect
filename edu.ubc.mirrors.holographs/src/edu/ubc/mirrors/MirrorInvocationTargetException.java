package edu.ubc.mirrors;

public class MirrorInvocationTargetException extends Exception {

    private static final long serialVersionUID = -1104487512570919030L;
    
    private final InstanceMirror exception;
    
    public MirrorInvocationTargetException(InstanceMirror exception) {
        this.exception = exception;
    }

    public InstanceMirror getTargetException() {
        return exception;
    }
}
