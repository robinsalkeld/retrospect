package edu.ubc.mirrors.mutable;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.wrapping.WrappingInstanceMirror;

public class MutableInstanceMirror extends WrappingInstanceMirror {

    public MutableInstanceMirror(MutableVirtualMachineMirror vm, InstanceMirror immutableMirror) {
        super(vm, immutableMirror);
    }
    
    @Override
    public String toString() {
        return "MutableInstanceMirror on " + wrapped;
    }
}
