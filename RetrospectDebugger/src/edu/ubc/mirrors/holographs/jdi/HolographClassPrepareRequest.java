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
