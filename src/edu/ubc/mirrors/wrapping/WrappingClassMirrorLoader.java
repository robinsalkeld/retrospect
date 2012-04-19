package edu.ubc.mirrors.wrapping;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;

public class WrappingClassMirrorLoader extends WrappingInstanceMirror implements ClassMirrorLoader {

    private final ClassMirrorLoader wrappedLoader;
    
    protected WrappingClassMirrorLoader(WrappingVirtualMachine vm, ClassMirrorLoader wrappedLoader) {
        super(vm, wrappedLoader);
        this.wrappedLoader = wrappedLoader;
    }

    @Override
    public ClassMirror findLoadedClassMirror(String name) {
        return vm.getWrappedClassMirror(wrappedLoader.findLoadedClassMirror(name));
    }

}
