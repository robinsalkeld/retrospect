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
package edu.ubc.mirrors.raw.nativestubs.sun.misc;

import java.nio.ByteBuffer;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class PerfStubs extends NativeStubs {

    public PerfStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public void registerNatives() {
	// No need to do anything
    }
    
    @StubMethod
    public InstanceMirror createLong(InstanceMirror perf, InstanceMirror name, int derp, int florp, long blong) {
	VirtualMachineHolograph vm = getVM();
	
	// Just create an arbitrary buffer - this VM doesn't expose any performance tracking API.
	ClassMirror byteBufferClass = vm.findBootstrapClassMirror(ByteBuffer.class.getName());
	MethodMirror method = HolographInternalUtils.getMethod(byteBufferClass, "allocate", vm.getPrimitiveClass("int"));
	return (InstanceMirror)HolographInternalUtils.mirrorInvoke(ThreadHolograph.currentThreadMirror(), method, null, 16);
    }
}
