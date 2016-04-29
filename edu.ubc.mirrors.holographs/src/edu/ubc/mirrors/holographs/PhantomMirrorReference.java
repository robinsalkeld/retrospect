package edu.ubc.mirrors.holographs;

import java.lang.ref.WeakReference;

import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.wrapping.WrappingMirror;

class PhantomMirrorReference extends WeakReference<ObjectMirror> {

    VirtualMachineHolograph vm;
    InstanceHolograph refMirror;
    ObjectMirror wrapped;
    
    public PhantomMirrorReference(VirtualMachineHolograph vm, InstanceHolograph refMirror, ObjectMirror referent) {
        super(referent);
        this.vm = vm;
        if (referent instanceof WrappingMirror) {
            wrapped = ((WrappingMirror)referent).getWrapped();
            vm.collectable.add(this);
        }
    }
}