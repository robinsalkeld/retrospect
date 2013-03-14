package edu.ubc.mirrors.eclipse.mat;

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
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.ArrayClassMirror;
import edu.ubc.mirrors.raw.PrimitiveClassMirror;

public class HeapDumpVirtualMachineMirror implements VirtualMachineMirror {

    private final ISnapshot snapshot;
    
    public HeapDumpVirtualMachineMirror(ISnapshot snapshot) {
        Reflection.checkNull(snapshot);
        this.snapshot = snapshot;
        initPrimitiveClasses();
    }
    
    private Map<String, HeapDumpClassMirror> bootstrapClasses;
    private Map<String, ClassMirror> primitiveClasses;
    
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
            
            registerPrimitiveClass("java.lang.Boolean", "boolean");
            registerPrimitiveClass("java.lang.Byte", "byte");
            registerPrimitiveClass("java.lang.Short", "short");
            registerPrimitiveClass("java.lang.Character", "char");
            registerPrimitiveClass("java.lang.Integer", "int");
            registerPrimitiveClass("java.lang.Long", "long");
            registerPrimitiveClass("java.lang.Float", "float");
            registerPrimitiveClass("java.lang.Double", "double");
            registerPrimitiveClass("java.lang.Void", "void");
        }
    }
    
    private void registerPrimitiveClass(String boxingTypeName, String typeName) {
        try {
            IClass boxingType = snapshot.getClassesByName(boxingTypeName, false).iterator().next();
            IInstance primitiveInstance = null;
            for (Field field : boxingType.getStaticFields()) {
                if (field.getName().equals("TYPE")) {
                    primitiveInstance = (IInstance)((ObjectReference)field.getValue()).getObject();
                    break;
                }
            }
            ClassMirror mirror = new PrimitiveClassMirror(this, typeName);
            primitiveClasses.put(typeName, mirror);
            mirrors.put(primitiveInstance, mirror);
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
}
