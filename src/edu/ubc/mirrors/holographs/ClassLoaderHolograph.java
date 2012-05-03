package edu.ubc.mirrors.holographs;

import java.util.HashMap;
import java.util.Map;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.BytecodeClassMirror;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.nativestubs.java.lang.SystemStubs;
import edu.ubc.mirrors.wrapping.WrappingClassMirrorLoader;

public class ClassLoaderHolograph extends WrappingClassMirrorLoader {

    private final Map<String, ClassHolograph> dynamicallyDefinedClasses =
            new HashMap<String, ClassHolograph>();
      
    protected ClassLoaderHolograph(VirtualMachineHolograph vm, ClassMirrorLoader wrappedLoader) {
        super(vm, wrappedLoader);
        this.mirageLoader = new MirageClassLoader(vm, this);
    }

    private final MirageClassLoader mirageLoader;
    
    public MirageClassLoader getMirageClassLoader() {
        return mirageLoader;
    }
    
    @Override
    public ClassMirror findLoadedClassMirror(String name) {
        ClassMirror result = super.findLoadedClassMirror(name);
        if (result != null) {
            return result;
        }
        
        return dynamicallyDefinedClasses.get(name);
    }
    
    @Override
    public ClassMirror defineClass1(String name, ByteArrayMirror b, int off, int len,
            InstanceMirror pd, InstanceMirror source, boolean verify) {
        
        if (findLoadedClassMirror(name) != null) {
            throw new IllegalArgumentException("Attempt to define already defined class: " + name);
        }
        
        final byte[] realBytecode = new byte[len];
        SystemStubs.arraycopyMirrors(b, off, new NativeByteArrayMirror(realBytecode), 0, len);
        
        ClassMirror newClass = new BytecodeClassMirror(name) {
            @Override
            public VirtualMachineMirror getVM() {
                return vm;
            }
            
            @Override
            public ClassMirrorLoader getLoader() {
                return wrappedLoader;
            }
            
            @Override
            protected ClassMirror loadClassMirrorInternal(String name) {
                ClassMirror classHolograph;
                try {
                    classHolograph = Reflection.classMirrorForName(ClassLoaderHolograph.this.vm, name, false, ClassLoaderHolograph.this);
                } catch (ClassNotFoundException e) {
                    // TODO-RS: This is actually wrong - this error should be caught when
                    // defining the class and raised to the caller!
                    throw new NoClassDefFoundError(e.getMessage());
                }
                return ((ClassHolograph)classHolograph).getWrappedClassMirror();
            }
            
            @Override
            public byte[] getBytecode() {
                return realBytecode;
            }
        };
        ClassHolograph newClassHolograph = (ClassHolograph)vm.getWrappedClassMirror(newClass);
        dynamicallyDefinedClasses.put(name, newClassHolograph);
        return newClassHolograph;
    }
}
