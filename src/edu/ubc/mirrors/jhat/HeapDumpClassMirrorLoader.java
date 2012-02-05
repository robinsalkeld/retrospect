package edu.ubc.mirrors.jhat;

import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IClassLoader;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.mirages.MirageClassLoader;

public class HeapDumpClassMirrorLoader extends ClassMirrorLoader {

    private final MirageClassLoader loader;
    private final IClassLoader classLoader;
    
    public HeapDumpClassMirrorLoader(MirageClassLoader loader, IClassLoader classLoader) {
        this.loader = loader;
        this.classLoader = classLoader;
    }
    
    public MirageClassLoader getMirageClassLoader() {
        return loader;
    }
    
    @Override
    public ClassMirror<?> loadClassMirror(String name) throws ClassNotFoundException {
        ClassMirror<?> mirror =  super.loadClassMirror(name);
        if (mirror != null) {
            return mirror;
        }
        
        List<IClass> classes;
        try {
            classes = classLoader.getDefinedClasses();
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
    
}
