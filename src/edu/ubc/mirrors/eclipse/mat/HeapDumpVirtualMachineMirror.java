package edu.ubc.mirrors.eclipse.mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.objectweb.asm.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.ClassLoaderHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.MethodHandle;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.ArrayClassMirror;
import edu.ubc.mirrors.raw.BytecodeClassMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.NativeVirtualMachineMirror;
import edu.ubc.mirrors.wrapping.VirtualMachineWrapperAware;
import edu.ubc.mirrors.wrapping.WrappingClassMirror;
import edu.ubc.mirrors.wrapping.WrappingVirtualMachine;

public class HeapDumpVirtualMachineMirror implements VirtualMachineMirror, VirtualMachineWrapperAware {

    private final ISnapshot snapshot;
    
    public HeapDumpVirtualMachineMirror(ISnapshot snapshot) {
        this.snapshot = snapshot;
    }
    
    private Map<String, HeapDumpClassMirror> bootstrapClasses;
    private VirtualMachineHolograph holographVM; 
    
    @Override
    public void setWrapper(VirtualMachineMirror wrapper) {
        if (wrapper instanceof VirtualMachineHolograph) {
            holographVM = (VirtualMachineHolograph)wrapper;
        }
    }
    
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
        final byte[] bytecode = NativeClassMirror.getNativeBytecode(holographVM.getBootstrapBytecodeLoader(), name);
        if (bytecode != null) {
            ClassMirror bytecodeClass = new BytecodeClassMirror(name) {
                @Override
                public byte[] getBytecode() {
                    return bytecode;
                }
                @Override
                public VirtualMachineMirror getVM() {
                    return HeapDumpVirtualMachineMirror.this;
                }
                @Override
                public ClassMirrorLoader getLoader() {
                    return null;
                }
                @Override
                protected ClassMirror loadClassMirrorInternal(Type type) {
                    try {
                        ClassMirror holographClass = Reflection.classMirrorForType(holographVM, type, false, null);
                        return getHeapDumpMirrorFromHolograph(holographClass);
                    } catch (ClassNotFoundException e) {
                        throw new NoClassDefFoundError(e.getMessage());
                    }
                }
                @Override
                public boolean initialized() {
                    return false;
                }
            };
            return new HeapDumpClassMirror(this, null, bytecodeClass);
        } 
        
        return null;
    }
    
    public ClassMirror getBytecodeClassMirror(final HeapDumpClassMirror snapshotClass) {
        final ClassMirror holographClass = snapshotClass.getWrapper();
        final VirtualMachineMirror holographVM = holographClass.getVM();
        final ClassMirrorLoader holographLoader = holographClass.getLoader();
        final byte[] bytecode = getBytecode(holographClass, snapshotClass);
        return new BytecodeClassMirror(snapshotClass.getClassName()) {
            @Override
            public byte[] getBytecode() {
                return bytecode;
            }
            @Override
            public VirtualMachineMirror getVM() {
                return HeapDumpVirtualMachineMirror.this;
            }
            @Override
            public ClassMirrorLoader getLoader() {
                return snapshotClass.getLoader();
            }
            @Override
            protected ClassMirror loadClassMirrorInternal(Type type) {
                try {
                    ClassMirror holographClass = Reflection.classMirrorForType(holographVM, type, false, holographLoader);
                    return getHeapDumpMirrorFromHolograph(holographClass);
                } catch (ClassNotFoundException e) {
                    throw new NoClassDefFoundError(e.getMessage());
                }
            }
            
            @Override
            public boolean initialized() {
                return false;
            }
        };
    }
    
    private ClassMirror getHeapDumpMirrorFromHolograph(ClassMirror holographClass) {
        if (holographClass.isArray()) {
            return new ArrayClassMirror(1, getHeapDumpMirrorFromHolograph(holographClass.getComponentClassMirror()));
        } else {
            ObjectMirror wrapped = ((ClassHolograph)holographClass).getWrapped();
            if (wrapped instanceof WrappingClassMirror) {
                WrappingClassMirror middleWrapper = (WrappingClassMirror)wrapped;
                return (ClassMirror)middleWrapper.getWrapped();
            } else {
                throw new NoClassDefFoundError(holographClass.getClassName());
            }
        }
    }
    
    protected ClassMirror wrapClassMirror(HeapDumpClassMirror heapDumpClassMirror) {
        WrappingVirtualMachine middle = (WrappingVirtualMachine)holographVM.getWrappedVM();
        return holographVM.getWrappedClassMirror(middle.getWrappedClassMirror(heapDumpClassMirror));
    }
    
    public byte[] getBytecode(ClassMirror holographClass, HeapDumpClassMirror snapshotClass) {
        ClassMirrorLoader holographLoader = holographClass.getLoader();
        if (holographLoader == null) {
            return NativeClassMirror.getNativeBytecode(holographVM.getBootstrapBytecodeLoader(), snapshotClass.getClassName());
        }
        
        String className = snapshotClass.getClassName();
        MirageClassLoader mirageLoader = ((ClassLoaderHolograph)holographLoader).getMirageClassLoader();
        if (mirageLoader.myTraceDir != null) {
            File file = mirageLoader.createClassFile(className.replace('.', '/') + ".original.class");
            if (file.exists()) {
                try {
                    return NativeClassMirror.readFully(new FileInputStream(file));
                } catch (Throwable e) {
                    throw new RuntimeException("Error caught while using cached original class definition " + className, e);
                }
            }
        }
        
        System.out.println("Fetching original bytecode for: " + snapshotClass.getClassName());
        
        VirtualMachineMirror holographVM = holographClass.getVM();
        ThreadMirror firstThread = holographVM.getThreads().get(0);
        
        String resourceName = className.replace('.', '/') + ".class";
        InstanceMirror resourceNameMirror = Reflection.makeString(holographVM, resourceName);
        InstanceMirror stream = (InstanceMirror)Reflection.invokeMethodHandle(firstThread, holographLoader, new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((ClassLoader)null).getResourceAsStream((String)null);
            }
        }, resourceNameMirror);
        if (stream == null) {
            throw new InternalError("Couldn't load bytecode for heap dump class: " + snapshotClass);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MethodHandle readMethod = new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((InputStream)null).read();
            }  
        };
        int b;
        // TODO-RS: Could be a lot faster if I got clever and tricky...
        while ((b = (Integer)Reflection.invokeMethodHandle(firstThread, stream, readMethod)) != -1) {
            baos.write(b);
        }
        byte[] result = baos.toByteArray();
        
        if (mirageLoader.myTraceDir != null) {
            File file = mirageLoader.createClassFile(className.replace('.', '/') + ".original.class");
            OutputStream classFile;
            try {
                classFile = new FileOutputStream(file);
                classFile.write(result);
                classFile.flush();
                classFile.close();
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
        return result;
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
        return new NativeClassMirror(NativeVirtualMachineMirror.getNativePrimitiveClass(name), this);
    }
    
    @Override
    public ClassMirror getArrayClass(int dimensions, ClassMirror elementClass) {
        return new ArrayClassMirror(dimensions, elementClass);
    }
}
