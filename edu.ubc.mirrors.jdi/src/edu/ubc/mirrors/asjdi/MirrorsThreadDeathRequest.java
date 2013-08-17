package edu.ubc.mirrors.asjdi;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.request.ThreadDeathRequest;

import edu.ubc.mirrors.ThreadMirrorDeathRequest;

public class MirrorsThreadDeathRequest extends MirrorsEventRequest implements ThreadDeathRequest {
    
    public MirrorsThreadDeathRequest(MirrorsVirtualMachine vm, ThreadMirrorDeathRequest wrapped) {
        super(vm, wrapped);
    }

    @Override
    public void addThreadFilter(ThreadReference arg1) {
        // TODO Auto-generated method stub
    }
}
