package edu.ubc.mirrors.wrapping;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
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
            unwrappedArgs[i] = vm.unwrappedValue(args[i]);
        }
        ObjectMirror unwrappedObj = vm.unwrapMirror(obj);
        ThreadMirror unwrappedThread = (ThreadMirror)vm.unwrapMirror(thread);
        Object result = wrapped.invoke(unwrappedThread, unwrappedObj, unwrappedArgs);
        return vm.wrapValue(result);
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

    @Override
    public ClassMirror getDeclaringClass() {
        return vm.getWrappedClassMirror(wrapped.getDeclaringClass());
    }

    @Override
    public int getSlot() {
        return wrapped.getSlot();
    }

    @Override
    public int getModifiers() {
        return wrapped.getModifiers();
    }

    @Override
    public List<ClassMirror> getExceptionTypes() {
        return vm.getWrappedClassMirrorList(wrapped.getExceptionTypes());
    }

    @Override
    public String getSignature() {
        // TODO-RS: Probably need to parse and re-build this too
        return wrapped.getSignature();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + wrapped;
    }
}
