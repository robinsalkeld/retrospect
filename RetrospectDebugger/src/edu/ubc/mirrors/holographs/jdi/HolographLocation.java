package edu.ubc.mirrors.holographs.jdi;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;

import edu.ubc.mirrors.mirages.MirageClassGenerator;

public class HolographLocation extends Holograph implements Location {

    private final Location wrapped;
    
    /**
     * @param o
     * @return
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        return wrapped.compareTo(((HolographLocation)o).wrapped);
    }

    public HolographLocation(JDIHolographVirtualMachine vm, Location location) {
        super(vm, location);
        this.wrapped = location;
    }
    
    /**
     * @return
     * @see com.sun.jdi.Location#codeIndex()
     */
    public long codeIndex() {
        return wrapped.codeIndex();
    }

    /**
     * @return
     * @see com.sun.jdi.Location#declaringType()
     */
    public ReferenceType declaringType() {
        return vm.wrapReferenceType(wrapped.declaringType());
    }

    /**
     * @return
     * @see com.sun.jdi.Location#lineNumber()
     */
    public int lineNumber() {
        return wrapped.lineNumber();
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.Location#lineNumber(java.lang.String)
     */
    public int lineNumber(String arg0) {
        return wrapped.lineNumber(arg0);
    }

    /**
     * @return
     * @see com.sun.jdi.Location#method()
     */
    public Method method() {
        return new HolographMethod(vm, wrapped.method());
    }

    /**
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.Location#sourceName()
     */
    public String sourceName() throws AbsentInformationException {
        return wrapped.sourceName();
    }

    /**
     * @param arg0
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.Location#sourceName(java.lang.String)
     */
    public String sourceName(String arg0) throws AbsentInformationException {
        return wrapped.sourceName(arg0);
    }

    /**
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.Location#sourcePath()
     */
    public String sourcePath() throws AbsentInformationException {
        return MirageClassGenerator.getOriginalInternalClassName(wrapped.sourcePath());
    }

    /**
     * @param arg0
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.Location#sourcePath(java.lang.String)
     */
    public String sourcePath(String arg0) throws AbsentInformationException {
        return wrapped.sourcePath(arg0);
    }

    /**
     * @return
     * @see com.sun.jdi.Mirror#toString()
     */
    public String toString() {
        return wrapped.toString();
    }
}
