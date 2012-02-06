package edu.ubc.mirrors.jhat;

import org.eclipse.mat.snapshot.model.IPrimitiveArray;

import edu.ubc.mirrors.BoxingArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.mirages.MirageClassLoader;

public class HeapDumpPrimitiveArrayMirror extends BoxingArrayMirror {
    
    private final MirageClassLoader loader;
    private final IPrimitiveArray array;
    
    public HeapDumpPrimitiveArrayMirror(MirageClassLoader loader, IPrimitiveArray array) {
        this.loader = loader;
        this.array = array;
    }
    
    public ClassMirror<?> getClassMirror() {
        return new HeapDumpClassMirror(loader, array.getClazz());
    }
    public int length() {
        // TODO Auto-generated method stub
        return 0;
    }
    public Object get(int index) {
        return ((IPrimitiveArray)array).getValueAt(index);
    }
    public void set(int index, Object o) {
        throw new UnsupportedOperationException();
    }
}
