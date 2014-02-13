package edu.ubc.mirrors.tod;

import java.util.HashSet;

import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventSet;

public class TODMirrorEventSet extends HashSet<MirrorEvent> implements MirrorEventSet {

    private final TODVirtualMachineMirror vm;
    
    private static final long serialVersionUID = 8455197622920137674L;

    public TODMirrorEventSet(TODVirtualMachineMirror vm) {
        this.vm = vm;
    }
    
    @Override
    public void resume() {
        // TODO-RS: Managing multiple threads correctly
        vm.resume();
    }
    
}
