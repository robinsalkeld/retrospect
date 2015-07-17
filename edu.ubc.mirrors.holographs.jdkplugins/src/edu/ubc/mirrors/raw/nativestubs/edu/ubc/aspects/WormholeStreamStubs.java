package edu.ubc.mirrors.raw.nativestubs.edu.ubc.aspects;

import java.io.IOException;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;

public class WormholeStreamStubs extends NativeStubs {

    public WormholeStreamStubs(ClassHolograph klass) {
        super(klass);
    }
    
    @StubMethod
    public void write(InstanceMirror mirror, ByteArrayMirror b, int off, int len) throws IOException {
        int fd = HolographInternalUtils.getIntField(mirror, "fd");
        byte[] nativeBytes = new byte[len];
        ArrayMirror nativeBytesMirror = new NativeByteArrayMirror(nativeBytes);
        Reflection.arraycopy(b, off, nativeBytesMirror, 0, len);
        if (fd == 1) {
            getVM().getSystemOut().write(nativeBytes);
        } else if (fd == 2) {
            getVM().getSystemErr().write(nativeBytes);
        } else {
            throw new InternalError("Illegal write to WormholeStream");
        }
    }
}
