package edu.ubc.mirrors.mutable;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.wrapping.WrappingClassMirror;

public class MutableClassMirror extends WrappingClassMirror {

    public MutableClassMirror(MutableVirtualMachineMirror vm, ClassMirror immutableClassMirror) {
        super(vm, immutableClassMirror);
    }
    
    public String toString() {
        return "MutableClassMirror on " + wrapped;
    };
}

