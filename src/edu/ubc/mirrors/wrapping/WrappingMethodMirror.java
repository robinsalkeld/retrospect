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
    public ObjectMirror invoke(InstanceMirror obj, ObjectMirror... args)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {

        ObjectMirror[] unwrappedArgs = new ObjectMirror[args.length];
        for (int i = 0; i < args.length; i++) {
            unwrappedArgs[i] = vm.unwrapMirror(args[i]);
        }
        InstanceMirror unwrappedObj = (InstanceMirror)vm.unwrapMirror(obj);
        ObjectMirror result = wrapped.invoke(unwrappedObj, unwrappedArgs);
        return vm.getWrappedMirror(result);
    }
    
}
