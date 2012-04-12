package edu.ubc.mirrors.eclipse.mat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IClassLoader;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.raw.NativeObjectMirror;
import edu.ubc.mirrors.raw.NativeVirtualMachineMirror;

public class HeapDumpVirtualMachineMirror implements VirtualMachineMirror {

    private final ISnapshot snapshot;
    
    private VirtualMachineMirror bytecodeVM;
    
    public HeapDumpVirtualMachineMirror(ISnapshot snapshot) {
        this.snapshot = snapshot;
    }
    
    private Map<String, HeapDumpClassMirror> bootstrapClasses;
     
    private void initBootstrapClasses() {
        if (bootstrapClasses == null) {
            bootstrapClasses = new HashMap<String, HeapDumpClassMirror>();
            try {
                for (IClass c : snapshot.getClasses()) {
                    if (c.getClassLoaderId() == 0) {
                        bootstrapClasses.put(c.getName(), new HeapDumpClassMirror(this, c));
                    }
                }
            } catch (SnapshotException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    @Override
    public ClassMirror findBootstrapClassMirror(String name) {
        initBootstrapClasses();
        ClassMirror result = bootstrapClasses.get(name);
        if (result != null) {
            return result;
        }
        
        // Fall back to the bytecode loader if the class wasn't previously loaded
        return bytecodeVM.findBootstrapClassMirror(name);
    }
    
    private final Map<IClassLoader, ClassMirrorLoader> bytecodeLoaders = 
          new HashMap<IClassLoader, ClassMirrorLoader>();
    
    public void addBytecodeLoader(IClassLoader snapshotLoader, ClassMirrorLoader bytecodeLoader) {
        bytecodeLoaders.put(snapshotLoader, bytecodeLoader);
    }
    
    public void addNativeBytecodeLoaders(IClassLoader snapshotLoader, ClassLoader bytecodeLoader) {
        bytecodeVM = NativeVirtualMachineMirror.INSTANCE;
        
        addBytecodeLoader(snapshotLoader, (ClassMirrorLoader)NativeObjectMirror.makeMirror(bytecodeLoader));
            
        IClassLoader parent;
        try {
            parent = (IClassLoader)snapshotLoader.resolveValue("parent");
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
        if (parent != null && parent != snapshotLoader) {
            addNativeBytecodeLoaders(parent, bytecodeLoader.getParent());
        }
        
        IClassLoader loaderLoader = HeapDumpClassMirror.getClassLoader(snapshotLoader.getClazz());
        if (loaderLoader != null) {
            addNativeBytecodeLoaders(loaderLoader, bytecodeLoader.getClass().getClassLoader());
        }
    }
    
    public ClassMirror getBytecodeClassMirror(HeapDumpClassMirror snapshotClass) {
        HeapDumpClassMirrorLoader snapshotLoader = snapshotClass.getLoader();
        if (snapshotLoader == null) {
            return bytecodeVM.findBootstrapClassMirror(snapshotClass.getClassName());
        } else {
            ClassMirrorLoader bytecodeLoader = bytecodeLoaders.get(snapshotLoader.heapDumpObject);
            if (bytecodeLoader == null) {
                throw new IllegalArgumentException("No bytecode loader found for: " + snapshotLoader);
            }
            // TODO-RS: Reflection.loadClassMirror() instead?
            return bytecodeLoader.findLoadedClassMirror(snapshotClass.getClassName());
        }
    }
    
    private static final Map<IObject, ObjectMirror> mirrors = new HashMap<IObject, ObjectMirror>();
    
    public ObjectMirror makeMirror(IObject object) {
        if (object == null) {
            return null;
        }
        ObjectMirror mirror = mirrors.get(object);
        if (mirror != null) {
            return mirror;
        }
        
        if (object instanceof IClass) {
            mirror = new HeapDumpClassMirror(this, (IClass)object);
        } else if (object instanceof IClassLoader) {
            mirror = new HeapDumpClassMirrorLoader(this, (IClassLoader)object);
        } else if (object.getClazz().getName().equals(Thread.class.getName())) {
            mirror = new HeapDumpThreadMirror(this, (IInstance)object);
        } else if (object instanceof IInstance) {
            if (object.getClazz().getName().equals(Class.class.getName())) {
                int whatthe = 4;
                whatthe++;
            }
            mirror = new HeapDumpInstanceMirror(this, (IInstance)object);
        } else if (object instanceof IPrimitiveArray) {
            mirror = new HeapDumpPrimitiveArrayMirror(this, (IPrimitiveArray)object);
        } else if (object instanceof IObjectArray) {
            mirror = new HeapDumpObjectArrayMirror(this, (IObjectArray)object);
        } else {
            throw new IllegalArgumentException("Unsupported subclass: " + object.getClass());
        }
        
        mirrors.put(object, mirror);
        return mirror;
    }
    
    @Override
    public List<ClassMirror> findAllClasses(String name) {
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        try {
            for (IClass klass : snapshot.getClassesByName(name, false)) {
                result.add((ClassMirror)makeMirror(klass));
            }
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
