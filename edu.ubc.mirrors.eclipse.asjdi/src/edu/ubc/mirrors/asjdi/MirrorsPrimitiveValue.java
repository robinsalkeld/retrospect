package edu.ubc.mirrors.asjdi;

import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.asjdi.MirrorsMirror;
import edu.ubc.mirrors.asjdi.MirrorsPrimitiveValue;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public abstract class MirrorsPrimitiveValue<T extends Number & Comparable<? super T>, U extends PrimitiveValue> extends MirrorsMirror implements PrimitiveValue {

    private final Type type;
    protected final T value;
    
    public MirrorsPrimitiveValue(MirrorsVirtualMachine vm, T wrapped) {
        super(vm, wrapped);
        ClassMirror primitiveClass = vm.vm.getPrimitiveClass(wrapped.getClass().getSimpleName().toLowerCase());
        this.type = vm.typeForClassMirror(primitiveClass);
        this.value = wrapped;
    }

    @Override
    public Type type() {
        return type;
    }
    
    @SuppressWarnings("unchecked")
    public int compareTo(Object o) {
        // This is not fully general, but much easier to implement for now...
        return value.compareTo(((MirrorsPrimitiveValue<T, ?>)o).value);
    };
    
    @Override
    public boolean booleanValue() {
        return value.byteValue() != 0;
    }

    @Override
    public byte byteValue() {
        return value.byteValue();
    }

    @Override
    public char charValue() {
        return (char)value.intValue();
    }

    @Override
    public double doubleValue() {
        return value.doubleValue();
    }

    @Override
    public float floatValue() {
        return value.floatValue();
    }

    @Override
    public int intValue() {
        return value.intValue();
    }

    @Override
    public long longValue() {
        return value.longValue();
    }

    @Override
    public short shortValue() {
        return value.shortValue();
    }
}
