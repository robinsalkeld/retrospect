package edu.ubc.mirrors.eclipse.mat;

import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;

import edu.ubc.mirrors.BoxingArrayMirror;
import edu.ubc.mirrors.ClassMirror;

public class HeapDumpPrimitiveArrayMirror extends BoxingArrayMirror implements HeapDumpObjectMirror {
    
    private final HeapDumpVirtualMachineMirror vm;
    private final IPrimitiveArray array;
    
    public HeapDumpPrimitiveArrayMirror(HeapDumpVirtualMachineMirror vm, IPrimitiveArray array) {
        this.vm = vm;
        this.array = array;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        
        return ((HeapDumpPrimitiveArrayMirror)obj).array.equals(array);
    }
    
    @Override
    public int hashCode() {
        return 11 * array.hashCode();
    }
    
    public HeapDumpClassMirror getClassMirror() {
        return vm.makeClassMirror(array.getClazz());
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

    @Override
    public IObject getHeapDumpObject() {
        return array;
    }
    
    @Override
    public int identityHashCode() {
        return vm.identityHashCode(array);
    }
}
