package edu.ubc.mirrors.wrapping;

import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;

public class WrappingClassMirrorLoader extends WrappingInstanceMirror implements ClassMirrorLoader {

    protected final ClassMirrorLoader wrappedLoader;
    
    protected WrappingClassMirrorLoader(WrappingVirtualMachine vm, ClassMirrorLoader wrappedLoader) {
        super(vm, wrappedLoader);
        this.wrappedLoader = wrappedLoader;
    }

    @Override
    public List<ClassMirror> loadedClassMirrors() {
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        for (ClassMirror klass : wrappedLoader.loadedClassMirrors()) {
            result.add(vm.getWrappedClassMirror(klass));
        }
        return result;
    }
    
    @Override
    public ClassMirror findLoadedClassMirror(String name) {
        return vm.getWrappedClassMirror(wrappedLoader.findLoadedClassMirror(name));
    }

    @Override
    public ClassMirror defineClass1(String name, ByteArrayMirror b,
            int off, int len, InstanceMirror pd, InstanceMirror source) {
        return vm.getWrappedClassMirror(wrappedLoader.defineClass1(name, b, off, len, pd, source));
    }
}
