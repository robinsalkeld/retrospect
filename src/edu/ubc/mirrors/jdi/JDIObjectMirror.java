package edu.ubc.mirrors.jdi;

import com.sun.jdi.ObjectReference;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectMirror;

public abstract class JDIObjectMirror implements ObjectMirror {

    protected final JDIVirtualMachineMirror vm;
    protected final ObjectReference mirror;

    public JDIObjectMirror(JDIVirtualMachineMirror vm, ObjectReference t) {
        this.vm = vm;
        this.mirror = t;
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
    public ClassMirror getClassMirror() {
        return vm.makeClassMirror(mirror.referenceType().classObject());
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + mirror;
    }
}
