package edu.ubc.mirrors.asjdi;

import com.sun.jdi.FloatValue;

import edu.ubc.mirrors.asjdi.MirrorsPrimitiveValue;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsFloatValue extends MirrorsPrimitiveValue<Float, FloatValue> implements FloatValue {

    public MirrorsFloatValue(MirrorsVirtualMachine vm, float value) {
        super(vm, value);
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public float value() {
        return value;
    }
}
