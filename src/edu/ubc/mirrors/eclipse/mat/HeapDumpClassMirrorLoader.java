package edu.ubc.mirrors.eclipse.mat;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IClassLoader;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.raw.BytecodeClassMirrorLoader;
import edu.ubc.mirrors.wrapping.WrapperAware;
import edu.ubc.mirrors.wrapping.WrappingMirror;

public class HeapDumpClassMirrorLoader extends HeapDumpInstanceMirror implements ClassMirrorLoader, WrapperAware {

    private final HeapDumpVirtualMachineMirror vm;
    private final Map<String, IClass> loadedClasses = new HashMap<String, IClass>();
    
    private ClassMirrorLoader wrapper;
    
    public HeapDumpClassMirrorLoader(HeapDumpVirtualMachineMirror vm, IClassLoader heapDumpClassLoader) {
        super(vm, heapDumpClassLoader);
        this.vm = vm;
        
        try {
            for (IClass klass : heapDumpClassLoader.getDefinedClasses()) {
                loadedClasses.put(HeapDumpClassMirror.getClassName(klass), klass);
            }
        } catch (SnapshotException e) {
            throw new InternalError();
        }
    }
    
    public VirtualMachineMirror getVM() {
        return vm;
    }
    
    @Override
    public HeapDumpClassMirror findLoadedClassMirror(String name) {
        IClass klass = loadedClasses.get(name);
        if (klass != null) {
            return new HeapDumpClassMirror(vm, klass);
        }

        return null;
    }
    
@Override
    public ClassMirror defineClass1(String name, ByteArrayMirror b, int off,
            int len, InstanceMirror pd, InstanceMirror source) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void setWrapper(WrappingMirror mirror) {
        this.wrapper = (ClassMirrorLoader)mirror;
    }
    
    public ClassMirrorLoader getWrapper() {
        return wrapper;
    }
}
