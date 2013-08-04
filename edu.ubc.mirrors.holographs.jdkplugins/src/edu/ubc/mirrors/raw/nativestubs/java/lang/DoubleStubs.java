package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class DoubleStubs extends NativeStubs {

    public DoubleStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public long doubleToRawLongBits(double value) {
        return Double.doubleToRawLongBits(value);
    }

    @StubMethod
    public double longBitsToDouble(long value) {
        return Double.longBitsToDouble(value);
    }
}
