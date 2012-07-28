package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.VirtualMachineMirror;

public interface VirtualMachineWrapperAware {

    public void setWrapper(VirtualMachineMirror wrapper);
}
