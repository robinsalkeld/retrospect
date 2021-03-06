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
package edu.ubc.mirrors.jdi;

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.EventRequestManager;

import edu.ubc.mirrors.AbstractMirrorEventRequest;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirrorGetRequest;

public class JDIFieldMirrorGetRequest extends AbstractMirrorEventRequest implements FieldMirrorGetRequest {

    protected final List<AccessWatchpointRequest> wrappedRequests = new ArrayList<AccessWatchpointRequest>();
    private final String declaringClass;
    private final String fieldName;
    
    public JDIFieldMirrorGetRequest(JDIVirtualMachineMirror vm, String declaringClass, String fieldName) {
        super(vm);
        this.declaringClass = declaringClass;
        this.fieldName = fieldName;

        EventRequestManager requestManager = vm.jdiVM.eventRequestManager();
        for (ReferenceType refType : vm.jdiVM.classesByName(declaringClass)) {
            Field field = refType.fieldByName(fieldName);
            if (field != null) {
                AccessWatchpointRequest wrapped = requestManager.createAccessWatchpointRequest(field);
                wrapped.putProperty(JDIEventRequest.MIRROR_WRAPPER, this);
                wrappedRequests.add(wrapped);
            }
        }
    }
    
    @Override
    public void addClassFilter(String classNamePattern) {
	for (AccessWatchpointRequest wrapped : wrappedRequests) {
            wrapped.addClassFilter(classNamePattern);
	}
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        for (AccessWatchpointRequest wrapped : wrappedRequests) {
            wrapped.setEnabled(enabled);
        }
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + declaringClass + "." + fieldName;
    }
}
