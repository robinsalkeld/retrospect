package edu.ubc.mirrors.jdi;

import static edu.ubc.mirrors.Reflection.checkNull;

import com.sun.jdi.Mirror;

public abstract class JDIMirror {
    
    protected final JDIVirtualMachineMirror vm;
    protected final Mirror mirror;

    public JDIMirror(JDIVirtualMachineMirror vm, Mirror mirror) {
	this.vm = checkNull(vm);
	this.mirror = checkNull(mirror);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        
        return mirror.equals(((JDIMirror)obj).mirror);
    }
    
    @Override
    public int hashCode() {
        return 11 * mirror.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + mirror;
    }
}
