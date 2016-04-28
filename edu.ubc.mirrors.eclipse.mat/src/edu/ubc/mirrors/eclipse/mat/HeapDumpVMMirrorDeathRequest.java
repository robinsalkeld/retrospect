package edu.ubc.mirrors.eclipse.mat;

import edu.ubc.mirrors.VMMirrorDeathRequest;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.fieldmap.FieldMapMirrorEventRequest;

public class HeapDumpVMMirrorDeathRequest extends FieldMapMirrorEventRequest implements VMMirrorDeathRequest {

    public HeapDumpVMMirrorDeathRequest(VirtualMachineMirror vm) {
        super(vm);
    }
}
