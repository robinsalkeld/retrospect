package edu.ubc.mirrors.eclipse.mat;

import org.eclipse.mat.snapshot.model.IInstance;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.raw.PrimitiveClassMirror;

public class HeapDumpPrimitiveClassMirror extends PrimitiveClassMirror {

    private final HeapDumpVirtualMachineMirror vm;
    private final IInstance klass;
    
    public HeapDumpPrimitiveClassMirror(HeapDumpVirtualMachineMirror vm, IInstance klass, String typeName, String signature) {
        super(vm, typeName, signature);
        this.vm = vm;
        this.klass = klass;
    }
    
    @Override
    public int identityHashCode() {
        return vm.identityHashCode(klass);
    }
}
