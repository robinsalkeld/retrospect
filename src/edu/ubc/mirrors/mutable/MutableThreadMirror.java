package edu.ubc.mirrors.mutable;

import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.wrapping.WrappingThreadMirror;

public class MutableThreadMirror extends WrappingThreadMirror {

    public MutableThreadMirror(MutableVirtualMachineMirror vm, ThreadMirror immutableThreadMirror) {
        super(vm, immutableThreadMirror);
    }
}
