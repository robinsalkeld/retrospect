package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.mirages.ObjectMirage;

public class NativeStubs {

    protected final ClassHolograph klass;
    
    public NativeStubs(ClassHolograph klass) {
	this.klass = klass;
    }

    protected VirtualMachineHolograph getVM() {
        return klass.getVM();
    }
    
    // Use in stubs methods to throw a mirror as an exception.
    // mirror.getClassMirror() must extend Throwable
    // TODO-RS: Change stubs to throw MirrorInvocationTargetExceptions instead so this isn't necessary 
    protected void throwMirror(InstanceMirror mirror) throws Throwable {
        throw (Throwable)ObjectMirage.make(mirror);
    }
}
