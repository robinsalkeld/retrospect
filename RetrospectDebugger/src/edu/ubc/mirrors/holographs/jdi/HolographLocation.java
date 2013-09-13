/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package edu.ubc.mirrors.holographs.jdi;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Locatable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;

import edu.ubc.mirrors.holograms.HologramClassGenerator;

public class HolographLocation extends Holograph implements Location {

    private final Location wrapped;
    
    /**
     * @param o
     * @return
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Locatable o) {
        return wrapped.compareTo(o);
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
        return HologramClassGenerator.getOriginalInternalClassName(wrapped.sourcePath());
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
