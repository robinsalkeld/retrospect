package edu.ubc.mirrors.eclipse.mat;

import edu.ubc.mirrors.ClassMirrorPrepareRequest;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.fieldmap.FieldMapMirrorEventRequest;

public class HeapDumpClassMirrorPrepareRequest extends FieldMapMirrorEventRequest implements ClassMirrorPrepareRequest {

    public HeapDumpClassMirrorPrepareRequest(VirtualMachineMirror vm) {
        super(vm);
    }


}
