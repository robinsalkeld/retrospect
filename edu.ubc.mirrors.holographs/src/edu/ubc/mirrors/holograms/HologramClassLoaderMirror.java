package edu.ubc.mirrors.holograms;

import static edu.ubc.mirrors.holograms.HologramClassGenerator.getOriginalBinaryClassName;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holograms.HologramClassGenerator;
import edu.ubc.mirrors.holograms.HologramClassMirror;
import edu.ubc.mirrors.holograms.HologramVirtualMachine;
import edu.ubc.mirrors.wrapping.WrappingClassMirrorLoader;

public class HologramClassLoaderMirror extends WrappingClassMirrorLoader {

    ClassMirrorLoader parent;
    
    public HologramClassLoaderMirror(HologramVirtualMachine vm, ClassMirrorLoader parent, ClassMirrorLoader originalLoader) {
        super(vm, originalLoader);
        this.parent = parent;
    }
    
    public ClassMirror findLoadedClassMirror(String name) {
        ClassMirror result = parent.findLoadedClassMirror(name);
        if (result != null) {
            return result;
        }
                    
        if (name.startsWith("hologram") && !name.startsWith("hologramarray")) {
            String originalClassName = getOriginalBinaryClassName(name);
            ClassMirror original = HolographInternalUtils.loadClassMirrorInternal(vm, wrappedLoader, originalClassName);
            return new HologramClassMirror((HologramVirtualMachine)vm, original, HologramClassGenerator.isImplementationClass(name));
        } else {
            return null;
        }
    }
}
