package edu.ubc.mirrors.asjdi;

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.Value;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.raw.nativestubs.java.lang.SystemStubs;

public class MirrorsArrayReference extends MirrorsObjectReference implements ArrayReference {

    private final ArrayMirror wrapped;
    
    public MirrorsArrayReference(MirrorsVirtualMachine vm, ArrayMirror wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public Value getValue(int index) {
        return vm.valueForObject(SystemStubs.getArrayElement(wrapped, index));
    }

    @Override
    public List<Value> getValues() {
        int length = length();
        List<Value> result = new ArrayList<Value>(length);
        for (int i = 0; i < length; i++) {
            result.add(getValue(length));
        }
        return result;
    }

    @Override
    public List<Value> getValues(int index, int length) {
        List<Value> result = new ArrayList<Value>(length);
        for (int i = 0; i < length; i++) {
            result.add(getValue(index + i));
        }
        return result;
    }

    @Override
    public int length() {
        return wrapped.length();
    }

    @Override
    public void setValue(int index, Value value) throws InvalidTypeException, ClassNotLoadedException {
        SystemStubs.setArrayElement(wrapped, index, vm.objectForValue(value));
    }

    @Override
    public void setValues(List<? extends Value> values) throws InvalidTypeException, ClassNotLoadedException {
        // TODO-RS: Bah, no time...
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValues(int arg0, List<? extends Value> arg1, int arg2, int arg3) throws InvalidTypeException, ClassNotLoadedException {
        // TODO-RS: Bah, no time...
        throw new UnsupportedOperationException();
    }

}
