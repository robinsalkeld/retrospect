package edu.ubc.mirrors.wrapping;

import java.lang.reflect.InvocationTargetException;

import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;

public class WrappingConstructorMirror implements ConstructorMirror {

    private final WrappingVirtualMachine vm;
    private final ConstructorMirror wrapped;
    
    public WrappingConstructorMirror(WrappingVirtualMachine vm, ConstructorMirror wrapped) {
        this.vm = vm;
        this.wrapped = wrapped;
    }
    
    @Override
    public InstanceMirror newInstance(Object... args)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, InstantiationException {

        Object[] unwrappedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            unwrappedArgs[i] = WrappingMethodMirror.getWrappedValue(vm, args[i]);
        }
        InstanceMirror result = wrapped.newInstance(unwrappedArgs);
        return (InstanceMirror)vm.getWrappedMirror(result);
    }
    
    @Override
    public void setAccessible(boolean flag) {
        wrapped.setAccessible(flag);
    }
}
