package edu.ubc.mirrors.fieldmap;

import edu.ubc.mirrors.AdviceMirrorHandlerRequest;
import edu.ubc.mirrors.VirtualMachineMirror;

public class DirectAdviceMirrorHandlerRequest extends FieldMapMirrorEventRequest implements AdviceMirrorHandlerRequest {

    public DirectAdviceMirrorHandlerRequest(VirtualMachineMirror vm) {
        super(vm);
    }

}
