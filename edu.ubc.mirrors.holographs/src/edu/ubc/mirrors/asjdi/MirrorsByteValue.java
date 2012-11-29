package edu.ubc.mirrors.asjdi;

import com.sun.jdi.ByteValue;

public class MirrorsByteValue extends MirrorsPrimitiveValue<Byte, ByteValue> implements ByteValue {

    public MirrorsByteValue(MirrorsVirtualMachine vm, byte value) {
        super(vm, value);
    }

    @Override
    public byte byteValue() {
        return value;
    }

    @Override
    public byte value() {
        return value;
    }
}
