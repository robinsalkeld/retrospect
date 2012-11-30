package edu.ubc.mirrors.asjdi;

import com.sun.jdi.LongValue;

public class MirrorsLongValue extends MirrorsPrimitiveValue<Long, LongValue> implements LongValue {

    public MirrorsLongValue(MirrorsVirtualMachine vm, long value) {
        super(vm, value);
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public long value() {
        return value;
    }
}
