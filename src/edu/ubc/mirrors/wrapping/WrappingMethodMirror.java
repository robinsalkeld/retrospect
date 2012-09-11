package edu.ubc.mirrors.wrapping;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;

public class WrappingMethodMirror implements MethodMirror {

    private final WrappingVirtualMachine vm;
    protected final MethodMirror wrapped;
    
    public WrappingMethodMirror(WrappingVirtualMachine vm, MethodMirror wrapped) {
        this.vm = vm;
        this.wrapped = wrapped;
    }
    
    @Override
    public Object invoke(ThreadMirror thread, ObjectMirror obj, Object... args)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {

        Object[] unwrappedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            unwrappedArgs[i] = unwrappedValue(vm, args[i]);
        }
        ObjectMirror unwrappedObj = vm.unwrapMirror(obj);
        ThreadMirror unwrappedThread = (ThreadMirror)vm.unwrapMirror(thread);
        Object result = wrapped.invoke(unwrappedThread, unwrappedObj, unwrappedArgs);
        return getWrappedValue(vm, result);
    }
    
    static Object getWrappedValue(WrappingVirtualMachine vm, Object value) {
        if (value instanceof ObjectMirror) {
            return vm.getWrappedMirror((ObjectMirror)value);
        } else {
            return value;
        }
    }
    
    static Object unwrappedValue(WrappingVirtualMachine vm, Object value) {
        if (value instanceof ObjectMirror) {
            return vm.unwrapMirror((ObjectMirror)value);
        } else {
            return value;
        }
    }
    
    @Override
    public void setAccessible(boolean flag) {
        wrapped.setAccessible(flag);
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public List<ClassMirror> getParameterTypes() {
        return vm.getWrappedClassMirrorList(wrapped.getParameterTypes());
    }

    @Override
    public ClassMirror getReturnType() {
        return vm.getWrappedClassMirror(wrapped.getReturnType());
    }
    
    @Override
    public byte[] getRawAnnotations() {
        return wrapped.getRawAnnotations();
    }

    @Override
    public byte[] getRawParameterAnnotations() {
        return wrapped.getRawParameterAnnotations();
    }

    @Override
    public byte[] getRawAnnotationDefault() {
        return wrapped.getRawAnnotationDefault();
    }
}
