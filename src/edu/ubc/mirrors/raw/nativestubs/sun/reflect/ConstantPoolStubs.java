package edu.ubc.mirrors.raw.nativestubs.sun.reflect;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.raw.ConstantPoolReader;

public class ConstantPoolStubs extends NativeStubs {

    public ConstantPoolStubs(ClassHolograph klass) {
	super(klass);
    }

    public Mirage getClassAt0(Mirage pool, Mirage reader, int index) {
        ConstantPoolReader poolReader = (ConstantPoolReader)reader.getMirror();
        ClassMirror classMirror = poolReader.getClassAt(index);
        return ObjectMirage.make(classMirror);
    }
    
    public Mirage getUTF8At0(Mirage pool, Mirage reader, int index) {
        ConstantPoolReader poolReader = (ConstantPoolReader)reader.getMirror();
        InstanceMirror sMirror = poolReader.getStringAt(index);
        return ObjectMirage.make(sMirror);
    }
}
