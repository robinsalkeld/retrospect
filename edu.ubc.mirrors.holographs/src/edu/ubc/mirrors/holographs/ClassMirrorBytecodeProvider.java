package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.ClassMirror;

public interface ClassMirrorBytecodeProvider {

    public byte[] getBytecode(ClassMirror classMirror);
}
