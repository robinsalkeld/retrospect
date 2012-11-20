package edu.ubc.mirrors.holographs;

import java.util.HashMap;
import java.util.Map;

import com.sun.jdi.request.ClassPrepareRequest;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ClassMirrorPrepareEvent;
import edu.ubc.mirrors.ClassMirrorPrepareRequest;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.EventDispatch.EventCallback;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.nativestubs.java.lang.SystemStubs;

public class ClassLoaderHolograph extends InstanceHolograph implements ClassMirrorLoader {

    protected final ClassMirrorLoader wrappedLoader;
    
    private final Map<String, ClassHolograph> dynamicallyDefinedClasses =
            new HashMap<String, ClassHolograph>();
      
    protected ClassLoaderHolograph(VirtualMachineHolograph vm, ClassMirrorLoader wrappedLoader) {
        super(vm, wrappedLoader);
        this.wrappedLoader = wrappedLoader;
        this.mirageLoader = new MirageClassLoader(vm, this);
    }

    private final MirageClassLoader mirageLoader;
    
    public MirageClassLoader getMirageClassLoader() {
        return mirageLoader;
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
        SystemStubs.arraycopyMirrors(b, off, new NativeByteArrayMirror(realBytecode), 0, len);
        
        ClassMirror newClass = new DefinedClassMirror(vm, this, name, realBytecode);
        final ClassHolograph newClassHolograph = (ClassHolograph)vm.getWrappedClassMirror(newClass);
        dynamicallyDefinedClasses.put(name, newClassHolograph);
        
        DefinedClassMirror.registerPrepareCallback(newClassHolograph);
        
        return newClassHolograph;
    }
}
