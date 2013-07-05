package edu.ubc.mirrors.asjdi;

import com.sun.jdi.ShortValue;

public class MirrorsShortValue extends MirrorsPrimitiveValue<Short, ShortValue> implements ShortValue {

    public MirrorsShortValue(MirrorsVirtualMachine vm, short value) {
        super(vm, value);
    }

    @Override
    public short shortValue() {
        return value;
    }

    @Override
    public short value() {
        return value;
    }
}