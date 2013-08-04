package edu.ubc.mirrors.raw.nativestubs.sun.reflect;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;
import edu.ubc.mirrors.raw.ConstantPoolReader;

public class ConstantPoolStubs extends NativeStubs {

    public ConstantPoolStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public ClassMirror getClassAt0(InstanceMirror pool, InstanceMirror reader, int index) {
        ConstantPoolReader poolReader = (ConstantPoolReader)reader;
        return poolReader.getClassAt(index);
    }
    
    @StubMethod
    public InstanceMirror getUTF8At0(InstanceMirror pool, InstanceMirror reader, int index) {
        ConstantPoolReader poolReader = (ConstantPoolReader)reader;
        return poolReader.getStringAt(index);
    }
}
