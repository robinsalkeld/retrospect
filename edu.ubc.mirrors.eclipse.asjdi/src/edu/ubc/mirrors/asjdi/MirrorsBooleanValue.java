package edu.ubc.mirrors.asjdi;

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;

import edu.ubc.mirrors.asjdi.MirrorsPrimitiveValue;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsBooleanValue extends MirrorsPrimitiveValue<Byte, ByteValue> implements BooleanValue {

    public MirrorsBooleanValue(MirrorsVirtualMachine vm, boolean value) {
        super(vm, value ? (byte)1 : 0);
    }

    @Override
    public boolean booleanValue() {
        return value.byteValue() == 0;
    }

    @Override
    public boolean value() {
        return booleanValue();
    }
}
