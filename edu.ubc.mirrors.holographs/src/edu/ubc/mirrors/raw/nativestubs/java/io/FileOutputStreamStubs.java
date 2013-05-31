package edu.ubc.mirrors.raw.nativestubs.java.io;

import java.io.IOException;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;

public class FileOutputStreamStubs extends NativeStubs {

    public FileOutputStreamStubs(ClassHolograph klass) {
	super(klass);
    }

    public void writeBytes(InstanceMirror fosMirror, ByteArrayMirror b, int off, int len, boolean append) throws IOException {
	InstanceMirror fdMirror = (InstanceMirror)HolographInternalUtils.getField(fosMirror, "fd");
	int fd = HolographInternalUtils.getIntField(fdMirror, "fd");
	byte[] nativeBytes = new byte[len];
	ArrayMirror nativeBytesMirror = new NativeByteArrayMirror(nativeBytes);
	Reflection.arraycopy(b, off, nativeBytesMirror, 0, len);
	if (fd == 1) {
	    System.out.write(nativeBytes);
	} else if (fd == 2) {
	    System.err.write(nativeBytes);
	} else {
	    throw new InternalError("Illegal write to FileOutputStream");
	}
    }
    
}
