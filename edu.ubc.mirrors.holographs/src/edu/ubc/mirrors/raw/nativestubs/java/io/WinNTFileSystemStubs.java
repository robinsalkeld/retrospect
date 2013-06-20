package edu.ubc.mirrors.raw.nativestubs.java.io;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;

public class WinNTFileSystemStubs extends FileSystemStubs {

    public WinNTFileSystemStubs(ClassHolograph klass) {
        super(klass);
    }
    
    public int getBooleanAttributes(InstanceMirror fs, InstanceMirror f) {
        return getBooleanAttributes0(fs, f);
    }
}
