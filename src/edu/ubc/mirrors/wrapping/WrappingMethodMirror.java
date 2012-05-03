package edu.ubc.mirrors.wrapping;

import java.lang.reflect.InvocationTargetException;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;

public class WrappingMethodMirror implements MethodMirror {

    private final WrappingVirtualMachine vm;
    private final MethodMirror wrapped;
    
    public WrappingMethodMirror(WrappingVirtualMachine vm, MethodMirror wrapped) {
        this.vm = vm;
        this.wrapped = wrapped;
    }
    
    @Override
    public Object invoke(InstanceMirror obj, Object... args)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {

        Object[] unwrappedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            unwrappedArgs[i] = unwrappedValue(vm, args[i]);
        }
        InstanceMirror unwrappedObj = (InstanceMirror)vm.unwrapMirror(obj);
        Object result = wrapped.invoke(unwrappedObj, unwrappedArgs);
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
}
