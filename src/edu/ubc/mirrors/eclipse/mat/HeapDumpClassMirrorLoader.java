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

    private final VirtualMachineMirror vm;
    
    private final ClassMirrorLoader bytecodeLoader;
    
    private final Map<String, IClass> loadedClasses = new HashMap<String, IClass>();
    
    public HeapDumpClassMirrorLoader(VirtualMachineMirror vm, ClassMirrorLoader bytecodeLoader, IClassLoader heapDumpClassLoader) {
        super(getClassLoader(vm, bytecodeLoader, heapDumpClassLoader), heapDumpClassLoader);
        this.vm = vm;
        this.bytecodeLoader = bytecodeLoader;
        
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
    
    private static HeapDumpClassMirrorLoader getClassLoader(VirtualMachineMirror vm, ClassMirrorLoader bytecodeLoader, IClassLoader heapDumpClassLoader) {
        try {
            
            IClassLoader loadersLoader = (IClassLoader)heapDumpClassLoader.getSnapshot().getObject(heapDumpClassLoader.getClazz().getClassLoaderId());
            if (loadersLoader.equals(heapDumpClassLoader)) {
                return null;
            } else {
                return new HeapDumpClassMirrorLoader(vm, bytecodeLoader.getClassMirror().getLoader(), loadersLoader);
            }
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
    }
    
//    public static HeapDumpClassMirrorLoader getParent(ClassMirrorLoader bytecodeLoader, IClassLoader heapDumpClassLoader) {
//        // The MAT model seems to create a phantom ClassLoader for the bootstrap class loader
//        if (heapDumpClassLoader.getObjectId() == 0) {
//            return null;
//        }
//        
//        IClassLoader parent;
//        try {
//            parent = (IClassLoader)heapDumpClassLoader.resolveValue("parent");
//            if (parent == null) {
//                parent = (IClassLoader)heapDumpClassLoader.getSnapshot().getObject(0);
//            }
//        } catch (SnapshotException e) {
//            throw new RuntimeException(e);
//        }
//        
//        return new HeapDumpClassMirrorLoader(bytecodeLoader.getParent(), parent);
//    }
    
    @Override
    public ClassMirror findLoadedClassMirror(String name) {
        IClass klass = loadedClasses.get(name);
        if (klass != null) {
            return new HeapDumpClassMirror(this, klass);
        }

        // Find using the bytecode loader if this class was not loaded already
        // but would have been accessible.
        return bytecodeLoader.findLoadedClassMirror(name);
    }
    
    public ClassMirrorLoader getBytecodeLoader() {
        return bytecodeLoader;
    }
}
