package edu.ubc.mirrors.holographs;

import java.lang.ref.WeakReference;

import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.wrapping.WrappingMirror;

class PhantomMirrorReference extends WeakReference<HolographicReference> {

    final VirtualMachineHolograph vm;
    final InstanceHolograph refMirror;
    ObjectMirror wrapped;
    
    public PhantomMirrorReference(VirtualMachineHolograph vm, InstanceHolograph refMirror, HolographicReference ref) {
        super(ref);
        this.vm = vm;
        this.refMirror = refMirror;
        ObjectMirror referent = ref.getReferent();
        if (referent instanceof WrappingMirror) {
            wrapped = ((WrappingMirror)referent).getWrapped();
            vm.collectable.add(this);
        }
    }
}