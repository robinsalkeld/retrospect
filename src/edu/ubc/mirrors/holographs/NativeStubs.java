package edu.ubc.mirrors.holographs;

public class NativeStubs {

    protected final ClassHolograph klass;
    
    public NativeStubs(ClassHolograph klass) {
	this.klass = klass;
    }

    protected VirtualMachineHolograph getVM() {
        return klass.getVM();
    }
}
