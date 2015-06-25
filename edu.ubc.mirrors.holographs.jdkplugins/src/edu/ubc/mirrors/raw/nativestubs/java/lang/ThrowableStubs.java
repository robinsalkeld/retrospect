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
package edu.ubc.mirrors.raw.nativestubs.java.lang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class ThrowableStubs extends NativeStubs {

    // TODO-RS: Would it be valid to store these in Throwable.backtrace instead?
    private final Map<InstanceMirror, List<FrameMirror>> stackTraces =
            new HashMap<InstanceMirror, List<FrameMirror>>();
    
    public ThrowableStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public InstanceMirror fillInStackTrace(InstanceMirror throwable) {
        List<FrameMirror> stackTrace = ThreadHolograph.currentThreadMirror().getStackTrace();
        int callingFrameIndex = 0;
        FrameMirror frame = stackTrace.get(callingFrameIndex);
        while (frame.declaringClass().getClassName().equals(Throwable.class.getName()) 
                && frame.methodName().equals("fillInStackTrace") || frame.methodName().equals("<init>")) {
            callingFrameIndex++;
            frame = stackTrace.get(callingFrameIndex);
        }

        stackTrace = stackTrace.subList(callingFrameIndex, stackTrace.size());
        stackTraces.put(throwable, stackTrace);

        // The native method has the side-effect of clearing the stackTrace field, so make sure the mirror does that too.
        HolographInternalUtils.setField(throwable, "stackTrace", null);

        return throwable;
    }
    
    // Java 1.7 version
    @StubMethod
    public InstanceMirror fillInStackTrace(InstanceMirror throwable, int dummy) {
	return fillInStackTrace(throwable);
    }
    
    @StubMethod
    public int getStackTraceDepth(InstanceMirror throwable) {
        List<FrameMirror> stackTrace = stackTraces.get(throwable);
        return stackTrace == null ? 0 : stackTrace.size();
    }
    
    @StubMethod
    public InstanceMirror getStackTraceElement(InstanceMirror throwable, int index) {
	VirtualMachineMirror vm = getVM();
        return HolographInternalUtils.stackTraceElementForFrameMirror(vm, stackTraces.get(throwable).get(index));
    }
    
}
