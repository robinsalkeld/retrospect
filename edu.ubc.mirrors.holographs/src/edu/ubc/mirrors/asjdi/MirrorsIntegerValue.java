package edu.ubc.mirrors.asjdi;

import com.sun.jdi.IntegerValue;

public class MirrorsIntegerValue extends MirrorsPrimitiveValue<Integer, IntegerValue> implements IntegerValue {

    public MirrorsIntegerValue(MirrorsVirtualMachine vm, int value) {
        super(vm, value);
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public int value() {
        return value;
    }
}
