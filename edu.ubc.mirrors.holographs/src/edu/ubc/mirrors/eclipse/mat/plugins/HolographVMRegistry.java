package edu.ubc.mirrors.eclipse.mat.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;

import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpObjectMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.wrapping.WrappingMirror;

public class HolographVMRegistry {

    private static final Map<ISnapshot, VirtualMachineHolograph> vms = new HashMap<ISnapshot, VirtualMachineHolograph>();
    private static final Map<IObject, ObjectMirror> mirrors = new HashMap<IObject, ObjectMirror>();
    
    public static VirtualMachineHolograph getHolographVM(ISnapshot snapshot) {
        VirtualMachineHolograph vm = vms.get(snapshot);
        if (vm == null) {
            HeapDumpVirtualMachineMirror hdvm = new HeapDumpVirtualMachineMirror(snapshot);
            
            // TODO-RS: proper configuration
            vm = new VirtualMachineHolograph(hdvm, Reflection.getBootstrapPath(),
                    Collections.singletonMap("/", "/"));
            
            vms.put(snapshot, vm);
        }
        return vm;
    }
    
    public static ObjectMirror getMirror(IObject object) {
        ObjectMirror mirror = mirrors.get(object);
        if (mirror == null) {
            VirtualMachineHolograph vm = getHolographVM(object.getSnapshot());
            HeapDumpVirtualMachineMirror hdvm = (HeapDumpVirtualMachineMirror)vm.getWrappedVM();
            mirror = vm.getWrappedMirror(hdvm.makeMirror(object));
            mirrors.put(object, mirror);
        }
        return mirror;
    }

    public static IObject fromMirror(ObjectMirror element) {
        return ((HeapDumpObjectMirror)((WrappingMirror)element).getWrapped()).getHeapDumpObject();
    }
    
}
