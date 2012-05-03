package edu.ubc.mirrors.holographs;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.wrapping.WrappingVirtualMachine;

public class VirtualMachineHolograph extends WrappingVirtualMachine {

    private final MirageClassLoader mirageLoader;
    
    public VirtualMachineHolograph(VirtualMachineMirror wrappedVM) {
        super(wrappedVM);
        this.mirageLoader = new MirageClassLoader(this, null);
    }
    
    @Override
    protected ObjectMirror wrapMirror(ObjectMirror mirror) {
        if (mirror instanceof ClassMirror) {
            return new ClassHolograph(this, (ClassMirror)mirror);
        } else if (mirror instanceof ClassMirrorLoader) {
            return new ClassLoaderHolograph(this, (ClassMirrorLoader)mirror);
        } else if (mirror instanceof InstanceMirror) {
            return new InstanceHolograph(this, (InstanceMirror)mirror);
        } else if (mirror.getClassMirror().getClassName().length() == 2) {
            // TODO-RS: wrapping primitive array mirrors
            return super.wrapMirror(mirror);
        } else if (mirror instanceof ObjectArrayMirror) {
            return new ObjectArrayHolograph(this, (ObjectArrayMirror)mirror);
        } else {
            return super.wrapMirror(mirror);
        }
    }
    
    public MirageClassLoader getMirageClassLoader() {
        return mirageLoader;
    }
}
