package edu.ubc.mirrors.raw.nativestubs.java.io;

import java.io.IOException;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.nativestubs.java.lang.SystemStubs;

public class FileOutputStreamStubs extends NativeStubs {

    public FileOutputStreamStubs(ClassHolograph klass) {
	super(klass);
    }

    public void writeBytes(Mirage fos, Mirage b, int off, int len, boolean append) throws IOException {
	InstanceMirror fosMirror = (InstanceMirror)fos.getMirror();
	InstanceMirror fdMirror = (InstanceMirror)HolographInternalUtils.getField(fosMirror, "fd");
	int fd = HolographInternalUtils.getIntField(fdMirror, "fd");
	byte[] nativeBytes = new byte[len];
	ArrayMirror nativeBytesMirror = new NativeByteArrayMirror(nativeBytes);
	SystemStubs.arraycopyMirrors(b.getMirror(), off, nativeBytesMirror, 0, len);
	if (fd == 1) {
	    System.out.write(nativeBytes);
	} else if (fd == 2) {
	    System.err.write(nativeBytes);
	} else {
	    throw new InternalError("Illegal write to FileOutputStream");
	}
    }
    
}
