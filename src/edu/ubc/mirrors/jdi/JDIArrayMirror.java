package edu.ubc.mirrors.jdi;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ShortValue;
import com.sun.jdi.Value;

import edu.ubc.mirrors.BoxingArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

public class JDIArrayMirror extends BoxingArrayMirror implements ObjectArrayMirror {

    private final JDIVirtualMachineMirror vm;
    private final ArrayReference array;
    
    public JDIArrayMirror(JDIVirtualMachineMirror vm, ArrayReference array) {
        this.vm = vm;
        this.array = array;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        
        return ((JDIArrayMirror)obj).array.equals(array);
    }
    
    @Override
    public int hashCode() {
        return 11 * array.hashCode();
    }
    
    @Override
    public int length() {
        return array.length();
    }

    @Override
    public ClassMirror getClassMirror() {
        return vm.makeClassMirror(array.referenceType().classObject());
    }

    @Override
    public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
        return vm.makeMirror((ObjectReference)array.getValue(index));
    }

    @Override
    protected Object getBoxedValue(int index) throws ArrayIndexOutOfBoundsException {
        Value value = array.getValue(index);
        if (value instanceof BooleanValue) {
            return ((BooleanValue)value).booleanValue();
        } else if (value instanceof ByteValue) {
            return ((ByteValue)value).byteValue();
        } else if (value instanceof CharValue) {
            return ((CharValue)value).charValue();
        } else if (value instanceof ShortValue) {
            return ((ShortValue)value).shortValue();
        } else if (value instanceof IntegerValue) {
            return ((IntegerValue)value).intValue();
        } else if (value instanceof LongValue) {
            return ((LongValue)value).longValue();
        } else if (value instanceof FloatValue) {
            return ((FloatValue)value).floatValue();
        } else if (value instanceof DoubleValue) {
            return ((DoubleValue)value).doubleValue();
        } else {
            throw new IllegalStateException("Non-primitive array: " + array);
        }
    }

    @Override
    public void set(int index, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected void setBoxedValue(int index, Object o) throws ArrayIndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }
}
