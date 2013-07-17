package edu.ubc.mirrors.holograms;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.ClassLoaderHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;

public class HologramVerifier extends BetterVerifier {

    private final HologramVirtualMachine vm;
    
    public HologramVerifier(VirtualMachineHolograph vm, ClassLoaderHolograph loader) {
        super(vm, loader);
        this.vm = new HologramVirtualMachine(vm);
    }

    @Override
    protected ClassMirror getClassMirror(Type t) {
        String className = t.getClassName();
        String tPackage = className.substring(0, className.lastIndexOf('.'));
        if (tPackage.equals("edu.ubc.mirrors") || tPackage.equals("edu.ubc.mirrors.holograms") || className.equals(Throwable.class.getName())) {
            return HologramClassMirror.getFrameworkClassMirror(className);
        }
        Type originalType = HologramClassGenerator.getOriginalType(t);
        boolean isImplementation = HologramClassGenerator.isImplementationClass(className);
        ClassHolograph klass = (ClassHolograph)super.getClassMirror(originalType);
        return new HologramClassMirror(vm, klass, isImplementation);
    }
    
}
