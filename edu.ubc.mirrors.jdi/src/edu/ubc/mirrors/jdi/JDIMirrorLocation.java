package edu.ubc.mirrors.jdi;

import com.sun.jdi.Location;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MirrorLocation;

public final class JDIMirrorLocation implements MirrorLocation {

    private final JDIVirtualMachineMirror vm;
    final Location jdiLoc;
    
    public JDIMirrorLocation(JDIVirtualMachineMirror vm, Location jdiLoc) {
        this.vm = vm;
        this.jdiLoc = jdiLoc;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        
        return jdiLoc.equals(((JDIMirrorLocation)obj).jdiLoc);
    }
    
    @Override
    public int hashCode() {
        return 11 * jdiLoc.hashCode();
    }
    
    public Location getWrapped() {
        return jdiLoc;
    }
    
    @Override
    public ClassMirror declaringClass() {
        return vm.makeClassMirror(jdiLoc.declaringType());
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + jdiLoc;
    }
}
