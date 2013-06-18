package edu.ubc.mirrors.holographs;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ClassMirrorPrepareEvent;
import edu.ubc.mirrors.ClassMirrorPrepareRequest;
import edu.ubc.mirrors.EventDispatch.EventCallback;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.raw.ArrayClassMirror;
import edu.ubc.mirrors.raw.BytecodeClassMirror;

public class DefinedClassMirror extends BytecodeClassMirror implements NewInstanceMirror {
    
    public DefinedClassMirror(VirtualMachineHolograph vm, ClassLoaderHolograph loader, String className, byte[] bytecode) {
        super(className);
        this.vm = vm;
        this.loader = loader;
        this.bytecode = bytecode;
    }

    private final VirtualMachineHolograph vm;
    private final ClassLoaderHolograph loader;
    private final byte[] bytecode;
    
    @Override
    public VirtualMachineMirror getVM() {
        return vm.getWrappedVM();
    }
    
    @Override
    public ClassMirrorLoader getLoader() {
        return loader == null ? null : (ClassMirrorLoader)loader.getWrapped();
    }
    
    @Override
    protected ClassMirror loadClassMirrorInternal(Type type) {
        // Need to use ThreadHolograph.currentThreadMirrorNoError() in case we are trying to load a bootstrap
        // class and don't have an active thread. This is safe since downstream methods will check for a valid
        // thread if they need one.
        ClassMirror classHolograph = HolographInternalUtils.classMirrorForType(vm, ThreadHolograph.currentThreadMirrorNoError(), type, false, loader);
        return unwrapClassMirror(classHolograph);
    }
    
    private ClassMirror unwrapClassMirror(ClassMirror klass) {
        if (klass instanceof ClassHolograph) {
            return ((ClassHolograph)klass).getWrappedClassMirror();
        } else if (klass.isArray()) {
            return new ArrayClassMirror(1, unwrapClassMirror(klass.getComponentClassMirror()));
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public byte[] getBytecode() {
        return bytecode;
    }
    
    @Override
    public boolean initialized() {
        return false;
    }
    
    public static void registerPrepareCallback(final ClassHolograph newClassHolograph) {
        // Set up a callback so that if the wrapped VM defines this same class later on,
        // we can replace the holograph version with the "real" one. 
        // TODO-RS: Make sure this is sound by checking the side-effects
        // in the class initialization method!!!
        VirtualMachineHolograph vm = newClassHolograph.getVM();
        if (vm.canBeModified()) {
            ClassMirrorPrepareRequest request = vm.eventRequestManager().createClassMirrorPrepareRequest();
            request.addClassFilter(newClassHolograph.getClassName());
            vm.dispatch().addCallback(request, new EventCallback() {
                @Override
                public void handle(MirrorEvent event) {
                    ClassMirrorPrepareEvent prepareEvent = (ClassMirrorPrepareEvent)event;
                    ClassHolograph prepared = (ClassHolograph)prepareEvent.classMirror();
                    // The name will match, but we have to check the class loader manually.
                    ClassMirrorLoader preparedLoader = prepared.getLoader();
                    ClassLoaderHolograph holographLoader = newClassHolograph.getLoader();
                    if (preparedLoader == null ? holographLoader == null : preparedLoader.equals(holographLoader)) {
                        newClassHolograph.setWrapped(prepared.getWrappedClassMirror());
                    }
                }
            });
            request.enable();
        }
    }
}
