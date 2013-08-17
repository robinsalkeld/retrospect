package edu.ubc.mirrors.asjdi;

import com.sun.jdi.DoubleValue;

import edu.ubc.mirrors.asjdi.MirrorsPrimitiveValue;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsDoubleValue extends MirrorsPrimitiveValue<Double, DoubleValue> implements DoubleValue {

    public MirrorsDoubleValue(MirrorsVirtualMachine vm, double value) {
        super(vm, value);
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public double value() {
        return value;
    }
}
