package edu.ubc.mirrors.holographs.jdi;

import com.sun.jdi.Mirror;
import com.sun.jdi.VirtualMachine;

public class Holograph implements Mirror {
    
    final Mirror wrapped;
    protected final JDIHolographVirtualMachine vm;
    
    public Holograph(JDIHolographVirtualMachine vm, Mirror wrapped) {
        this.vm = vm;
        this.wrapped = wrapped;
    }

    @Override
    public final VirtualMachine virtualMachine() {
        return vm;
    }

    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof Holograph)) {
            return false;
        }
        
        return wrapped.equals(((Holograph)obj).wrapped);
    }
    
    @Override
    public final int hashCode() {
        return wrapped.hashCode() + 7;
    }
}
