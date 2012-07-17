package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.BoxingFieldMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectMirror;

/**
 * Special case for java.lang.System.security, which is hidden from the reflective API.
 * 
 * @author robinsalkeld
 *
 */
public class NativeSystemSecurityField extends BoxingFieldMirror {

    public static final NativeSystemSecurityField INSTANCE = new NativeSystemSecurityField();
    
    @Override
    public String getName() {
        return "security";
    }

    @Override
    public ClassMirror getType() {
        return (ClassMirror)NativeInstanceMirror.makeMirror(System.class);
    }

    @Override
    public ObjectMirror get() throws IllegalAccessException {
        return NativeInstanceMirror.makeMirror(System.getSecurityManager());
    }

    @Override
    public void set(ObjectMirror o) throws IllegalAccessException {
        Object nativeValue = ((NativeInstanceMirror)o).getNativeObject();
        System.setSecurityManager((SecurityManager)nativeValue);
    }

    @Override
    public Object getBoxedValue() throws IllegalAccessException {
        return get();
    }

    @Override
    public void setBoxedValue(Object o) throws IllegalAccessException {
        set((ObjectMirror)o);
    }

}
