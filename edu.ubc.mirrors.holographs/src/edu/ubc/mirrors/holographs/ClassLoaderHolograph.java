package edu.ubc.mirrors.holographs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holograms.HologramClassLoader;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;

public class ClassLoaderHolograph extends InstanceHolograph implements ClassMirrorLoader {

    protected final ClassMirrorLoader wrappedLoader;
    
    private final Map<String, ClassHolograph> dynamicallyDefinedClasses =
            new HashMap<String, ClassHolograph>();
      
    protected ClassLoaderHolograph(VirtualMachineHolograph vm, ClassMirrorLoader wrappedLoader) {
        super(vm, wrappedLoader);
        this.wrappedLoader = wrappedLoader;
        this.hologramLoader = new HologramClassLoader(vm, this);
    }

    private final HologramClassLoader hologramLoader;
    
    public HologramClassLoader getHologramClassLoader() {
        return hologramLoader;
    }
    
    @Override
    public List<ClassMirror> loadedClassMirrors() {
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        for (ClassMirror klass : wrappedLoader.loadedClassMirrors()) {
            result.add(vm.getWrappedClassMirror(klass));
        }
        result.addAll(dynamicallyDefinedClasses.values());
        return result;
    }
    
    @Override
    public ClassMirror findLoadedClassMirror(String name) {
        ClassMirror result = wrappedLoader.findLoadedClassMirror(name);
        if (result != null) {
            return vm.getWrappedClassMirror(result);
        }
        
        return dynamicallyDefinedClasses.get(name);
    }
    
    @Override
    public ClassMirror defineClass1(String name, ByteArrayMirror b, int off, int len,
            InstanceMirror pd, InstanceMirror source) {
        
        if (findLoadedClassMirror(name) != null) {
            throw new IllegalArgumentException("Attempt to define already defined class: " + name);
        }
        
        final byte[] realBytecode = new byte[len];
        Reflection.arraycopy(b, off, new NativeByteArrayMirror(realBytecode), 0, len);
        
        ClassMirror newClass = new DefinedClassMirror(vm, this, name, realBytecode);
        final ClassHolograph newClassHolograph = (ClassHolograph)vm.getWrappedClassMirror(newClass);
        dynamicallyDefinedClasses.put(name, newClassHolograph);
        
        DefinedClassMirror.registerPrepareCallback(newClassHolograph);
        
        return newClassHolograph;
    }
}
