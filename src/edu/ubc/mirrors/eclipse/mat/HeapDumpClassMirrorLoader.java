package edu.ubc.mirrors.eclipse.mat;

import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IClassLoader;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.mirages.MirageClassLoader;

public class HeapDumpClassMirrorLoader extends ClassMirrorLoader {

    private final ClassLoader classLoader;
    private final IClassLoader heapDumpClassLoader;
    
    public HeapDumpClassMirrorLoader(ClassLoader classLoader, IClassLoader heapDumpClassLoader) {
        this.classLoader = classLoader;
        this.heapDumpClassLoader = heapDumpClassLoader;
    }
    
    public HeapDumpClassMirrorLoader(ClassMirrorLoader parent, ClassLoader classLoader, IClassLoader heapDumpClassLoader) {
        super(parent);
        this.classLoader = classLoader;
        this.heapDumpClassLoader = heapDumpClassLoader;
    }
    
    @Override
    public ClassMirror loadClassMirror(String name) throws ClassNotFoundException {
        try {
           return super.loadClassMirror(name);
        } catch (ClassNotFoundException e) {
           // ignore
        }
        
        List<IClass> classes;
        try {
            classes = heapDumpClassLoader.getDefinedClasses();
        } catch (SnapshotException e) {
            throw new InternalError();
        }
        for (IClass klass : classes) {
            if (klass.getName().equals(name)) {
                return new HeapDumpClassMirror(this, klass);
            }
        }
        
        throw new ClassNotFoundException(name);
    }
    
    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
