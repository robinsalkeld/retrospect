package edu.ubc.mirrors.holographs.jdi;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;

import edu.ubc.mirrors.MethodHandle;

public class HolographStringReference extends HolographObjectReference implements StringReference {

    public HolographStringReference(JDIHolographVirtualMachine vm, ObjectReference wrapped) {
        super(vm, wrapped);
    }

    @Override
    public String value() {
        Value string = vm.invokeMethodHandle(wrapped, new MethodHandle(){
            protected void methodCall() throws Throwable {
                ((Object)null).toString();
            }
        });
        return ((StringReference)string).value();
    }

}
