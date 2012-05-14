package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectMirror;

public class NativeObjectMirror implements ObjectMirror {

    private final Object object;
    
    public NativeObjectMirror(Object object) {
        if (object == null) {
            throw new NullPointerException();
        }
        this.object = object;
    }
    
    public Object getNativeObject() {
        return object;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NativeObjectMirror)) {
            return false;
        }
        
        return object == ((NativeObjectMirror)obj).object;
    }
    
    @Override
    public int hashCode() {
        return 17 + System.identityHashCode(object);
    }
    
    public ClassMirror getClassMirror() {
        return new NativeClassMirror(object.getClass());
    }
}
