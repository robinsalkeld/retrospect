package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.wrapping.WrappingThreadMirror;
import edu.ubc.mirrors.wrapping.WrappingVirtualMachine;

public class ThreadHolograph extends WrappingThreadMirror {

    public ThreadHolograph(WrappingVirtualMachine vm, ThreadMirror wrappedThread) {
        super(vm, wrappedThread);
    }

}
