package edu.ubc.mirrors.holographs.jdkplugins;

import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;

public class NativeStubs {

    protected final ClassHolograph klass;
    
    public NativeStubs(ClassHolograph klass) {
	this.klass = klass;
    }

    protected VirtualMachineHolograph getVM() {
        return klass.getVM();
    }
}
