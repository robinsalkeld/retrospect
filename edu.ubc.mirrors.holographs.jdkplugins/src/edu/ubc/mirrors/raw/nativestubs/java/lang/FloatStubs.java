package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class FloatStubs extends NativeStubs {

    public FloatStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public int floatToRawIntBits(float value) {
        return Float.floatToRawIntBits(value);
    }

    @StubMethod
    public float intBitsToFloat(int value) {
        return Float.intBitsToFloat(value);
    }
}
