package edu.ubc.mirrors.eclipse.mat;

import org.eclipse.mat.snapshot.model.IPrimitiveArray;

import edu.ubc.mirrors.BoxingArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class HeapDumpPrimitiveArrayMirror extends BoxingArrayMirror {
    
    private final HeapDumpVirtualMachineMirror vm;
    private final IPrimitiveArray array;
    
    public HeapDumpPrimitiveArrayMirror(HeapDumpVirtualMachineMirror vm, IPrimitiveArray array) {
        this.vm = vm;
        this.array = array;
    }
    
    public ClassMirror getClassMirror() {
        return new HeapDumpClassMirror(vm, array.getClazz());
    }
    public int length() {
        return array.getLength();
    }
    public Object getBoxedValue(int index) {
        return ((IPrimitiveArray)array).getValueAt(index);
    }
    public void setBoxedValue(int index, Object o) {
        throw new UnsupportedOperationException();
    }
}
