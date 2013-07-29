package edu.ubc.mirrors.eclipse.mat;

import edu.ubc.mirrors.ThreadMirrorStartRequest;
import edu.ubc.mirrors.fieldmap.FieldMapMirrorEventRequest;

public class HeapDumpThreadMirrorStartRequest extends FieldMapMirrorEventRequest implements ThreadMirrorStartRequest {

    public HeapDumpThreadMirrorStartRequest(HeapDumpVirtualMachineMirror vm) {
        super(vm);
    }
}
