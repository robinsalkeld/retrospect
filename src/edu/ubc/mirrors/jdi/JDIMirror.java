package edu.ubc.mirrors.jdi;

import static edu.ubc.mirrors.mirages.Reflection.checkNull;

import com.sun.jdi.Mirror;

public class JDIMirror {
    
    protected final JDIVirtualMachineMirror vm;
    protected final Mirror mirror;

    public JDIMirror(JDIVirtualMachineMirror vm, Mirror mirror) {
	this.vm = checkNull(vm);
	this.mirror = checkNull(mirror);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JDIObjectMirror)) {
            return false;
        }
        
        return mirror.equals(((JDIObjectMirror)obj).mirror);
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
