package edu.ubc.mirrors.wrapping;

import java.lang.reflect.InvocationTargetException;

import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;

public class WrappingConstructorMirror implements ConstructorMirror {

    private final WrappingVirtualMachine vm;
    private final ConstructorMirror wrapped;
    
    public WrappingConstructorMirror(WrappingVirtualMachine vm, ConstructorMirror wrapped) {
        this.vm = vm;
        this.wrapped = wrapped;
    }
    
    @Override
    public InstanceMirror newInstance(ObjectMirror... args)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, InstantiationException {

        ObjectMirror[] unwrappedArgs = new ObjectMirror[args.length];
        for (int i = 0; i < args.length; i++) {
            unwrappedArgs[i] = vm.unwrapMirror(args[i]);
        }
        InstanceMirror result = wrapped.newInstance(unwrappedArgs);
        return (InstanceMirror)vm.getWrappedMirror(result);
    }
    
}
