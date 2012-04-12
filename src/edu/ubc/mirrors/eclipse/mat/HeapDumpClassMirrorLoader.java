package edu.ubc.mirrors.eclipse.mat;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IClassLoader;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.VirtualMachineMirror;

public class HeapDumpClassMirrorLoader extends HeapDumpInstanceMirror implements ClassMirrorLoader {

    private final HeapDumpVirtualMachineMirror vm;
    private final Map<String, IClass> loadedClasses = new HashMap<String, IClass>();
    
    public HeapDumpClassMirrorLoader(HeapDumpVirtualMachineMirror vm, IClassLoader heapDumpClassLoader) {
        super(vm, heapDumpClassLoader);
        this.vm = vm;
        
        try {
            for (IClass klass : heapDumpClassLoader.getDefinedClasses()) {
                loadedClasses.put(klass.getName(), klass);
            }
        } catch (SnapshotException e) {
            throw new InternalError();
        }
    }
    
    public VirtualMachineMirror getVM() {
        return vm;
    }
    
    @Override
    public ClassMirror findLoadedClassMirror(String name) {
        IClass klass = loadedClasses.get(name);
        if (klass != null) {
            return new HeapDumpClassMirror(vm, klass);
        } else {
            return null;
        }
    }
}
