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
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.raw.ArrayClassMirror;
import edu.ubc.mirrors.raw.BytecodeClassMirrorLoader;
import edu.ubc.mirrors.raw.BytecodeOnlyVirtualMachineMirror;
import edu.ubc.mirrors.raw.NativeInstanceMirror;
import edu.ubc.mirrors.raw.NativeVirtualMachineMirror;
import edu.ubc.mirrors.test.Breakpoint;

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
    public HeapDumpClassMirror findBootstrapClassMirror(String name) {
        initBootstrapClasses();
        HeapDumpClassMirror result = bootstrapClasses.get(name);
        if (result != null) {
            return result;
        }
        
        // If the class wasn't loaded already, imitate what the VM would have done to define it.
        // TODO-RS: Not completely sure how incomplete this is. Class transformers may still
        // apply etc.
        ClassMirror bytecodeClass = bytecodeVM.findBootstrapClassMirror(name);
        if (bytecodeClass != null) {
            return new HeapDumpClassMirror(this, null, bytecodeClass);
        } 
        
        return null;
    }
    
    private final Map<IClassLoader, ClassMirrorLoader> bytecodeLoaders = 
          new HashMap<IClassLoader, ClassMirrorLoader>();
    private final Map<ClassMirrorLoader, IClassLoader> loadersForBytecodeLoaders = 
          new HashMap<ClassMirrorLoader, IClassLoader>();
      
    public void addBytecodeLoader(IClassLoader snapshotLoader, ClassMirrorLoader bytecodeLoader) {
        bytecodeLoaders.put(snapshotLoader, bytecodeLoader);
        loadersForBytecodeLoaders.put(bytecodeLoader, snapshotLoader);
    }
    
    public VirtualMachineMirror getBytecodeVM() {
        return bytecodeVM;
    }
    
    public void setBytecodeVM(VirtualMachineMirror vm) {
        this.bytecodeVM = vm;
    }
    
    public void addNativeBytecodeLoaders(IClassLoader snapshotLoader, ClassLoader bytecodeLoader) {
//        BytecodeOnlyVirtualMachineMirror vm = new BytecodeOnlyVirtualMachineMirror(NativeVirtualMachineMirror.INSTANCE, null);
        VirtualMachineMirror vm = NativeVirtualMachineMirror.INSTANCE;
        setBytecodeVM(vm);
        
        addNativeBytecodeLoadersHelper(vm, snapshotLoader, bytecodeLoader);
    }
    
    public void addNativeBytecodeLoadersHelper(VirtualMachineMirror vm, IClassLoader snapshotLoader, ClassLoader bytecodeLoader) {
        addBytecodeLoader(snapshotLoader, (ClassMirrorLoader)NativeInstanceMirror.makeMirror(bytecodeLoader));
//        addBytecodeLoader(snapshotLoader, new BytecodeClassMirrorLoader(vm, bytecodeLoader));
            
        IClassLoader parent;
        try {
            parent = (IClassLoader)snapshotLoader.resolveValue("parent");
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
        if (parent != null && parent != snapshotLoader) {
            addNativeBytecodeLoadersHelper(vm, parent, bytecodeLoader.getParent());
        }
        
        IClassLoader loaderLoader = HeapDumpClassMirror.getClassLoader(snapshotLoader.getClazz());
        if (loaderLoader != null) {
            addNativeBytecodeLoadersHelper(vm, loaderLoader, bytecodeLoader.getClass().getClassLoader());
        }
    }
    
    public ClassMirrorLoader getBytecodeLoader(HeapDumpClassMirrorLoader loader) {
        ClassMirrorLoader bytecodeLoader = bytecodeLoaders.get(loader.heapDumpObject);
        if (bytecodeLoader == null) {
            throw new IllegalArgumentException("No bytecode loader found for: " + loader);
        }
        return bytecodeLoader;
    }
    
    public ClassMirror getBytecodeClassMirror(HeapDumpClassMirror snapshotClass) {
        HeapDumpClassMirrorLoader snapshotLoader = snapshotClass.getLoader();
        ClassMirror result;
        String className = snapshotClass.getClassName();
        if (snapshotLoader == null) {
            result = bytecodeVM.findBootstrapClassMirror(className);
        } else {
            ClassMirrorLoader bytecodeLoader = getBytecodeLoader(snapshotLoader);
            result = bytecodeLoader.findLoadedClassMirror(className);
        }
        return result;
    }
    
    public HeapDumpClassMirror getClassMirrorForBytecodeClassMirror(ClassMirror bytecodeClass) {
        ClassMirrorLoader bytecodeLoader = bytecodeClass.getLoader();
        if (bytecodeLoader == null) {
            return findBootstrapClassMirror(bytecodeClass.getClassName());
        } else {
            IClassLoader snapshotLoader = loadersForBytecodeLoaders.get(bytecodeLoader);
            if (snapshotLoader == null) {
                throw new IllegalArgumentException("No snapshot loader found for: " + bytecodeLoader);
            }
            return new HeapDumpClassMirrorLoader(this, snapshotLoader).findLoadedClassMirror(bytecodeClass.getClassName());
        }
    }
    
    private static final Map<IObject, ObjectMirror> mirrors = new HashMap<IObject, ObjectMirror>();
    
    public ObjectMirror makeMirror(IObject object) {
        if (object == null) {
            return null;
        }
        if (object.getObjectId() == 0) {
            // This is the fake ClassLoader MAT creates to represent the bootstrap loader.
            // It's not a valid object in several ways (it claims to be an instance of the
            // abstract class java.lang.ClassLoader, for one thing).
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
    public List<ClassMirror> findAllClasses(String name, boolean includeSubclasses) {
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        try {
            for (IClass klass : snapshot.getClassesByName(name, includeSubclasses)) {
                result.add((ClassMirror)makeMirror(klass));
            }
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    
    @Override
    public List<ThreadMirror> getThreads() {
        ClassMirror threadMirror = findBootstrapClassMirror(Thread.class.getName());
        List<InstanceMirror> instances = threadMirror.getInstances();
        List<ThreadMirror> threads = new ArrayList<ThreadMirror>(instances.size());
        for (InstanceMirror instance : instances) {
            threads.add((ThreadMirror)instance);
        }
        return threads;
    }
    
    
    
    @Override
    public ClassMirror getPrimitiveClass(String name) {
        // TODO-RS: Doesn't seem to be a way to get these, but this shouldn't be
        // an issue unless we start explicitly checking for VM mismatches like the JDI does.
        return NativeVirtualMachineMirror.INSTANCE.getPrimitiveClass(name);
    }
    
    @Override
    public ClassMirror getArrayClass(int dimensions, ClassMirror elementClass) {
        return new ArrayClassMirror(dimensions, elementClass);
    }
}
