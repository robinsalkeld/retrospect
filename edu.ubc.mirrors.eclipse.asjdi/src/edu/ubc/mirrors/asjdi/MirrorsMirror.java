package edu.ubc.mirrors.asjdi;

import com.sun.jdi.Mirror;
import com.sun.jdi.VirtualMachine;

import edu.ubc.mirrors.asjdi.MirrorsMirror;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public abstract class MirrorsMirror implements Mirror {

    protected final MirrorsVirtualMachine vm;
    protected final Object wrapped;
    
    public MirrorsMirror(MirrorsVirtualMachine vm, Object wrapped) {
        this.vm = vm;
        this.wrapped = wrapped;
    }

    @Override
    public VirtualMachine virtualMachine() {
        return vm;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        
        return ((MirrorsMirror)obj).wrapped.equals(wrapped);
    }
    
    @Override
    public int hashCode() {
        return 47 * wrapped.hashCode();
    }
}
