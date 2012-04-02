package edu.ubc.mirrors.eclipse.mat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IClassLoader;
import org.objectweb.asm.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.raw.ArrayClassMirror;

public class HeapDumpClassMirrorLoader extends ClassMirrorLoader {

    private final ClassMirrorLoader bytecodeLoader;
    
    private final IClassLoader heapDumpClassLoader;
    
    private final Map<String, IClass> loadedClasses = new HashMap<String, IClass>();
    
    public HeapDumpClassMirrorLoader(ClassMirrorLoader bytecodeLoader, IClassLoader heapDumpClassLoader) {
        super(getParent(bytecodeLoader, heapDumpClassLoader));
        this.bytecodeLoader = bytecodeLoader;
        this.heapDumpClassLoader = heapDumpClassLoader;
        
        try {
            for (IClass klass : heapDumpClassLoader.getDefinedClasses()) {
                loadedClasses.put(klass.getName(), klass);
            }
        } catch (SnapshotException e) {
            throw new InternalError();
        }
    }
    
    public static HeapDumpClassMirrorLoader getParent(ClassMirrorLoader bytecodeLoader, IClassLoader heapDumpClassLoader) {
        // The MAT model seems to create a phantom ClassLoader for the bootstrap class loader
        if (heapDumpClassLoader.getObjectId() == 0) {
            return null;
        }
        
        IClassLoader parent;
        try {
            parent = (IClassLoader)heapDumpClassLoader.resolveValue("parent");
            if (parent == null) {
                parent = (IClassLoader)heapDumpClassLoader.getSnapshot().getObject(0);
            }
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
        
        return new HeapDumpClassMirrorLoader(bytecodeLoader.getParent(), parent);
    }
    
    @Override
    public ClassMirror loadClassMirror(String name) throws ClassNotFoundException {
        try {
            return super.loadClassMirror(name);
        } catch (ClassNotFoundException e) {
            // Ignore
        }
    
        IClass klass = loadedClasses.get(name);
        if (klass != null) {
            return new HeapDumpClassMirror(this, klass);
        }

        // Find using the bytecode loader if this class was not loaded already
        // but would have been accessible.
        return bytecodeLoader.loadClassMirror(name);
    }
    
    public ClassMirrorLoader getBytecodeLoader() {
        return bytecodeLoader;
    }
}
