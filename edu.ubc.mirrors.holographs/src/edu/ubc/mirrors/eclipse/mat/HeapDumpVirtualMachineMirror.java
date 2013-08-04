package edu.ubc.mirrors.eclipse.mat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IClassLoader;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;
import org.eclipse.mat.snapshot.model.ObjectReference;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.raw.ArrayClassMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.PrimitiveClassMirror;

public class HeapDumpVirtualMachineMirror implements VirtualMachineMirror {

    private final ISnapshot snapshot;
    
    private Map<String, HeapDumpClassMirror> bootstrapClasses;
    private Map<String, ClassMirror> primitiveClasses;
    
    private Map<String, String> cachedBytecode;
    private File bytecodeMappingPath;
    
    public HeapDumpVirtualMachineMirror(ISnapshot snapshot) {
        Reflection.checkNull(snapshot);
        this.snapshot = snapshot;
        initPrimitiveClasses();
        initCachedBytecode();
    }
    
    private void initCachedBytecode() {
        String snapshotPath = snapshot.getSnapshotInfo().getPath();
        int lastDot = snapshotPath.lastIndexOf('.');
        bytecodeMappingPath = new File(snapshotPath.substring(0, lastDot) + "_bytecode.ini");
        cachedBytecode = VirtualMachineHolograph.readStringMapFromFile(bytecodeMappingPath);
    }

    public byte[] locateBytecode(HeapDumpClassMirror classMirror) {
        String path = cachedBytecode.get(Integer.toString(classMirror.klass.getObjectId()));
        if (path == null) {
            return null;
        }
        
        try {
            return NativeClassMirror.readFully(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void initBootstrapClasses() {
        if (bootstrapClasses == null) {
            bootstrapClasses = new HashMap<String, HeapDumpClassMirror>();
            try {
                for (IClass c : snapshot.getClasses()) {
                    if (c.getClassLoaderId() == 0) {
                        HeapDumpClassMirror classMirror = makeClassMirror(c);
                        bootstrapClasses.put(classMirror.getClassName(), classMirror);
                    }
                }
            } catch (SnapshotException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private void initPrimitiveClasses() {
        if (primitiveClasses == null) {
            primitiveClasses = new HashMap<String, ClassMirror>();
            
            registerPrimitiveClass("java.lang.Boolean", "boolean", "Z");
            registerPrimitiveClass("java.lang.Byte", "byte", "B");
            registerPrimitiveClass("java.lang.Short", "short", "S");
            registerPrimitiveClass("java.lang.Character", "char", "C");
            registerPrimitiveClass("java.lang.Integer", "int", "I");
            registerPrimitiveClass("java.lang.Long", "long", "J");
            registerPrimitiveClass("java.lang.Float", "float", "F");
            registerPrimitiveClass("java.lang.Double", "double", "D");
            registerPrimitiveClass("java.lang.Void", "void", "V");
        }
    }
    
    private void registerPrimitiveClass(String boxingTypeName, String typeName, String signature) {
        try {
            IClass boxingType = snapshot.getClassesByName(boxingTypeName, false).iterator().next();
            IInstance primitiveInstance = null;
            for (Field field : boxingType.getStaticFields()) {
                if (field.getName().equals("TYPE")) {
                    ObjectReference objectReference = (ObjectReference)field.getValue();
                    if (objectReference != null) {
                        primitiveInstance = (IInstance)objectReference.getObject();
                        break;
                    }
                }
            }
            ClassMirror mirror = new PrimitiveClassMirror(this, typeName, signature);
            primitiveClasses.put(typeName, mirror);
            if (primitiveInstance != null) {
                mirrors.put(primitiveInstance, mirror);
            }
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public HeapDumpClassMirror findBootstrapClassMirror(String name) {
        initBootstrapClasses();
        return bootstrapClasses.get(name);
    }

    @Override
    public ClassMirror defineBootstrapClass(String name, ByteArrayMirror b, int off, int len) {
        throw new UnsupportedOperationException();
    }
    
    private final Map<IObject, ObjectMirror> mirrors = new HashMap<IObject, ObjectMirror>();
    
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
        } else if (isThread(object)) {
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
    
    public HeapDumpClassMirror makeClassMirror(IClass klass) {
        return (HeapDumpClassMirror)makeMirror(klass);
    }
    
    private boolean isThread(IObject object) {
        IClass klass = object.getClazz();
        while (klass != null) {
            if (klass.getName().equals(Thread.class.getName())) {
                return true;
            }
            klass = klass.getSuperClass();
        }
        return false;
    }
    
    @Override
    public List<ClassMirror> findAllClasses() {
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        try {
            Collection<IClass> matches = snapshot.getClasses();
            if (matches != null) {
                for (IClass klass : matches) {
                    result.add((ClassMirror)makeMirror(klass));
                }
            }
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    
    @Override
    public List<ClassMirror> findAllClasses(String name, boolean includeSubclasses) {
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        try {
            Collection<IClass> matches = snapshot.getClassesByName(name, includeSubclasses);
            if (matches != null) {
                for (IClass klass : matches) {
                    result.add((ClassMirror)makeMirror(klass));
                }
            }
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    
    @Override
    public List<ThreadMirror> getThreads() {
        ClassMirror threadMirror = findBootstrapClassMirror(Thread.class.getName());
        List<ObjectMirror> instances = threadMirror.getInstances();
        List<ThreadMirror> threads = new ArrayList<ThreadMirror>(instances.size());
        for (ObjectMirror instance : instances) {
            threads.add((ThreadMirror)instance);
        }
        return threads;
    }
    
    @Override
    public ClassMirror getPrimitiveClass(String name) {
        return primitiveClasses.get(name);
    }
    
    @Override
    public ClassMirror getArrayClass(int dimensions, ClassMirror elementClass) {
        return new ArrayClassMirror(dimensions, elementClass);
    }

    @Override
    public MirrorEventRequestManager eventRequestManager() {
        return new HeapDumpEventRequestManager(this);
    }

    @Override
    public MirrorEventQueue eventQueue() {
	return new HeapDumpEventQueue(this); 
    }

    @Override
    public void resume() {
	throw new UnsupportedOperationException(); 
    }
    
    @Override
    public void suspend() {
	throw new UnsupportedOperationException(); 
    }
    
    @Override
    public boolean canBeModified() {
        return false;
    }
    
    @Override
    public boolean canGetBytecodes() {
        return false;
    }

    @Override
    public boolean hasClassInitialization() {
        return false;
    }
    
    int identityHashCode(IObject object) {
        return System.identityHashCode(object);
    }
    
    public ISnapshot getSnapshot() {
        return snapshot;
    }

    public void bytecodeLocated(HeapDumpClassMirror heapDumpClassMirror, File originalBytecodeLocation) {
        String key = Integer.toString(heapDumpClassMirror.klass.getObjectId());
        String value = originalBytecodeLocation.getAbsolutePath();
        cachedBytecode.put(key, value);
        VirtualMachineHolograph.addEntryToStringMapFile(bytecodeMappingPath, key, value);
    }
    
    static boolean equalObjects(HeapDumpObjectMirror obj, Object obj2) {
        if (obj2 == null || !obj.getClass().equals(obj2.getClass())) {
            return false;
        }
        
        // The heap dump object only compares object IDs, but doesn't check the
        // snapshot they come from, so make sure we check the VM.
        HeapDumpObjectMirror other = (HeapDumpObjectMirror)obj2;
        return obj.getClassMirror().getVM().equals(other.getClassMirror().getVM()) 
                && obj.getHeapDumpObject().equals(other.getHeapDumpObject());
    }
}
