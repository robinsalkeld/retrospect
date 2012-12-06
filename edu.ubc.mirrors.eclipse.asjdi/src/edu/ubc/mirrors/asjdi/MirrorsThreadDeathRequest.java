package edu.ubc.mirrors.asjdi;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.InvalidRequestStateException;
import com.sun.jdi.request.ThreadDeathRequest;

import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.ThreadMirrorDeathRequest;

public class MirrorsThreadDeathRequest extends MirrorsEventRequest implements ThreadDeathRequest {

    private final ThreadMirrorDeathRequest wrapped;
    
    public MirrorsThreadDeathRequest(MirrorsVirtualMachine vm, ThreadMirrorDeathRequest wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public void addThreadFilter(ThreadReference arg1) {
        // TODO Auto-generated method stub
    }
}
