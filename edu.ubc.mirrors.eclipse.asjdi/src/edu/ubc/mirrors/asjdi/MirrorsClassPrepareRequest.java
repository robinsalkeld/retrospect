package edu.ubc.mirrors.asjdi;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.ClassPrepareRequest;

import edu.ubc.mirrors.ClassMirrorPrepareRequest;

public class MirrorsClassPrepareRequest extends MirrorsEventRequest implements ClassPrepareRequest {

    private final ClassMirrorPrepareRequest wrapped;
    
    public MirrorsClassPrepareRequest(MirrorsVirtualMachine vm, ClassMirrorPrepareRequest wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public void addClassExclusionFilter(String arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addClassFilter(ReferenceType arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addClassFilter(String classNamePattern) {
        wrapped.addClassFilter(classNamePattern);
    }

    @Override
    public void addSourceNameFilter(String arg1) {
        throw new UnsupportedOperationException();
    }

}
