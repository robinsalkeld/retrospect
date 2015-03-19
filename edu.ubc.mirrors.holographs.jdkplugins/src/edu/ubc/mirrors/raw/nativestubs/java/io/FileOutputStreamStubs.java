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
package edu.ubc.mirrors.raw.nativestubs.java.io;

import java.io.IOException;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;

public class FileOutputStreamStubs extends NativeStubs {

    public FileOutputStreamStubs(ClassHolograph klass) {
	super(klass);
    }
    
    // Java 1.6
    @StubMethod
    public void writeBytes(InstanceMirror fosMirror, ByteArrayMirror b, int off, int len) throws IOException {
        writeBytes(fosMirror, b, off, len, true);
    }
    
    // Java 1.7
    @StubMethod
    public void writeBytes(InstanceMirror fosMirror, ByteArrayMirror b, int off, int len, boolean append) throws IOException {
	InstanceMirror fdMirror = (InstanceMirror)HolographInternalUtils.getField(fosMirror, "fd");
	int fd = HolographInternalUtils.getIntField(fdMirror, "fd");
	byte[] nativeBytes = new byte[len];
	ArrayMirror nativeBytesMirror = new NativeByteArrayMirror(nativeBytes);
	Reflection.arraycopy(b, off, nativeBytesMirror, 0, len);
	if (fd == 1) {
	    getVM().getSystemOut().write(nativeBytes);
	} else if (fd == 2) {
	    getVM().getSystemErr().write(nativeBytes);
	} else {
	    throw new InternalError("Illegal write to FileOutputStream");
	}
    }
    
}
