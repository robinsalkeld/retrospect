package edu.ubc.mirrors.holograms;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holograms.HologramClassMirror;
import edu.ubc.mirrors.wrapping.WrappingVirtualMachine;

public class HologramVirtualMachine extends WrappingVirtualMachine {

    public HologramVirtualMachine(VirtualMachineMirror wrappedVM) {
        super(wrappedVM);
    }

    @Override
    protected ObjectMirror wrapMirror(ObjectMirror mirror) {
        if (mirror instanceof ClassMirror) {
            return new HologramClassMirror(this, (ClassMirror)mirror, true);
        } else {
            return super.wrapMirror(mirror);
        }
    }
}
