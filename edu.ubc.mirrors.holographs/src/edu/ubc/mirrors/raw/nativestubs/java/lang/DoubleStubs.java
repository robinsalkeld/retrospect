package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;

public class DoubleStubs extends NativeStubs {

    public DoubleStubs(ClassHolograph klass) {
	super(klass);
    }

    public long doubleToRawLongBits(double value) {
        return Double.doubleToRawLongBits(value);
    }

    public double longBitsToDouble(long value) {
        return Double.longBitsToDouble(value);
    }
}
