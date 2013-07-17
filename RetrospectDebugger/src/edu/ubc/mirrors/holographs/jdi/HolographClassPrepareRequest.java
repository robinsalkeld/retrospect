package edu.ubc.mirrors.holographs.jdi;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.ClassPrepareRequest;

import edu.ubc.mirrors.holograms.HologramClassGenerator;

public class HolographClassPrepareRequest extends HolographEventRequest implements ClassPrepareRequest {

    private final ClassPrepareRequest wrapped;
    
    /**
     * @param arg1
     * @see com.sun.jdi.request.ClassPrepareRequest#addClassExclusionFilter(java.lang.String)
     */
    public void addClassExclusionFilter(String arg1) {
        wrapped.addClassExclusionFilter(arg1);
    }

    /**
     * @param arg1
     * @see com.sun.jdi.request.ClassPrepareRequest#addClassFilter(com.sun.jdi.ReferenceType)
     */
    public void addClassFilter(ReferenceType arg1) {
        wrapped.addClassFilter(arg1);
    }

    /**
     * @param arg1
     * @see com.sun.jdi.request.ClassPrepareRequest#addClassFilter(java.lang.String)
     */
    public void addClassFilter(String arg1) {
        wrapped.addClassFilter(HologramClassGenerator.getHologramBinaryClassName(arg1, true));
    }

    /**
     * @param arg1
     * @see com.sun.jdi.request.ClassPrepareRequest#addSourceNameFilter(java.lang.String)
     */
    public void addSourceNameFilter(String arg1) {
        wrapped.addSourceNameFilter(arg1);
    }

    public HolographClassPrepareRequest(JDIHolographVirtualMachine vm, ClassPrepareRequest wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

}
