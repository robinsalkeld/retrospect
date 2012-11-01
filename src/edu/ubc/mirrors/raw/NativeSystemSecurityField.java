package edu.ubc.mirrors.raw;

import java.lang.reflect.Modifier;

import edu.ubc.mirrors.BoxingFieldMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
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
    public ClassMirror getDeclaringClass() {
        return (ClassMirror)NativeInstanceMirror.make(System.class);
    }
    
    @Override
    public String getName() {
        return "security";
    }

    @Override
    public ClassMirror getType() {
        return (ClassMirror)NativeInstanceMirror.makeMirror(System.class);
    }

    @Override
    public int getModifiers() {
        return Modifier.PRIVATE | Modifier.STATIC | Modifier.VOLATILE;
    }
    
    @Override
    public ObjectMirror get(InstanceMirror obj) throws IllegalAccessException {
        return NativeInstanceMirror.makeMirror(System.getSecurityManager());
    }

    @Override
    public void set(InstanceMirror obj, ObjectMirror o) throws IllegalAccessException {
        Object nativeValue = ((NativeInstanceMirror)o).getNativeObject();
        System.setSecurityManager((SecurityManager)nativeValue);
    }

    @Override
    public Object getBoxedValue(InstanceMirror obj) throws IllegalAccessException {
        return get(obj);
    }

    @Override
    public void setBoxedValue(InstanceMirror obj, Object o) throws IllegalAccessException {
        set(obj, (ObjectMirror)o);
    }

}
