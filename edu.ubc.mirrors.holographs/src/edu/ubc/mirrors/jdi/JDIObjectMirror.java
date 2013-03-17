package edu.ubc.mirrors.jdi;

import com.sun.jdi.ObjectReference;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectMirror;

public abstract class JDIObjectMirror extends JDIMirror implements ObjectMirror {

    protected final ObjectReference mirror;

    public JDIObjectMirror(JDIVirtualMachineMirror vm, ObjectReference t) {
	super(vm, t);
        this.mirror = t;
    }

    @Override
    public ClassMirror getClassMirror() {
        return vm.makeClassMirror(mirror.referenceType().classObject());
    }
 
    @Override
    public int identityHashCode() {
        return vm.identityHashCode(mirror);
    }
}
