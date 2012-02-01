package edu.ubc.mirrors.jhat;

import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IClassLoader;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;

public class HeapDumpClassMirrorLoader extends ClassMirrorLoader {

    IClassLoader classLoader;
    
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
                return new HeapDumpClassMirror(klass);
            }
        }
        
        throw new ClassNotFoundException(name);
    }
    
}
