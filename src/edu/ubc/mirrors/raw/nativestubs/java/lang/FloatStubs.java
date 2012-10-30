package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;

public class FloatStubs extends NativeStubs {

    public FloatStubs(ClassHolograph klass) {
	super(klass);
    }

    public int floatToRawIntBits(float value) {
        return Float.floatToRawIntBits(value);
    }

    public float intBitsToFloat(int value) {
        return Float.intBitsToFloat(value);
    }
}
