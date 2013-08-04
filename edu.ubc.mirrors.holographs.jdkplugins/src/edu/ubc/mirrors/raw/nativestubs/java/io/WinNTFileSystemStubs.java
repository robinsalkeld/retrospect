package edu.ubc.mirrors.raw.nativestubs.java.io;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class WinNTFileSystemStubs extends FileSystemStubs {

    public WinNTFileSystemStubs(ClassHolograph klass) {
        super(klass);
    }
    
    @StubMethod
    public int getBooleanAttributes(InstanceMirror fs, InstanceMirror f) {
        return getBooleanAttributes0(fs, f);
    }
}
