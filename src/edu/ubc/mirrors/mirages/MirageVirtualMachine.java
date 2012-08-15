package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.wrapping.WrappingVirtualMachine;

public class MirageVirtualMachine extends WrappingVirtualMachine {

    public MirageVirtualMachine(VirtualMachineMirror wrappedVM) {
        super(wrappedVM);
    }

    @Override
    protected ObjectMirror wrapMirror(ObjectMirror mirror) {
        if (mirror instanceof ClassMirror) {
            return new MirageClassMirror(this, (ClassMirror)mirror, true);
        } else {
            return super.wrapMirror(mirror);
        }
    }
}
