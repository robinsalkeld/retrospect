package edu.ubc.mirrors.eclipse.mat;

import edu.ubc.mirrors.ThreadMirrorDeathRequest;
import edu.ubc.mirrors.fieldmap.FieldMapMirrorEventRequest;

public class HeapDumpThreadMirrorDeathRequest extends FieldMapMirrorEventRequest implements ThreadMirrorDeathRequest {

    public HeapDumpThreadMirrorDeathRequest(HeapDumpVirtualMachineMirror vm) {
        super(vm);
    }


}
