package edu.ubc.mirrors.holographs;

public class IllegalSideEffectError extends RuntimeException {

    private static final long serialVersionUID = -8689548914975934532L;

    public IllegalSideEffectError() {
        super();
    }
    
    public IllegalSideEffectError(String message) {
        super(message);
    }
    
    public IllegalSideEffectError(String message, Throwable cause) {
        super(message, cause);
    }
    
    public IllegalSideEffectError(Throwable cause) {
        super(cause);
    }
}