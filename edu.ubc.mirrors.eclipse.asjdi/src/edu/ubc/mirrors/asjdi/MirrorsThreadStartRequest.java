package edu.ubc.mirrors.asjdi;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.request.ThreadStartRequest;

import edu.ubc.mirrors.fieldmap.FieldMapMirrorEventRequest;

public class MirrorsThreadStartRequest extends MirrorsEventRequest implements ThreadStartRequest {

    public MirrorsThreadStartRequest(MirrorsVirtualMachine vm) {
        super(vm, new FieldMapMirrorEventRequest(vm.vm));
    }

    @Override
    public void addThreadFilter(ThreadReference arg1) {
        // TODO Auto-generated method stub
    }

}
