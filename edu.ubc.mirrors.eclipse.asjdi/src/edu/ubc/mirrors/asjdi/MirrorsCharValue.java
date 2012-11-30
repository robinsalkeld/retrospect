package edu.ubc.mirrors.asjdi;

import com.sun.jdi.CharValue;
import com.sun.jdi.IntegerValue;

import edu.ubc.mirrors.asjdi.MirrorsPrimitiveValue;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsCharValue extends MirrorsPrimitiveValue<Integer, IntegerValue> implements CharValue {

    public MirrorsCharValue(MirrorsVirtualMachine vm, char value) {
        super(vm, (int)value);
    }

    @Override
    public int compareTo(Object o) {
        return value.compareTo(((CharValue)o).intValue());
    }
    
    @Override
    public char charValue() {
        return (char)value.intValue();
    }

    @Override
    public char value() {
        return charValue();
    }
}
