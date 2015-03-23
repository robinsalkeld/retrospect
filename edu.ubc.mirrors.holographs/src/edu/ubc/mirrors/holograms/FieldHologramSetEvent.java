package edu.ubc.mirrors.holograms;

import java.util.List;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;

public class FieldHologramSetEvent implements MirrorInvocationHandler {

    private final VirtualMachineHolograph vm;
    protected final FieldMirror field;
    
    public FieldHologramSetEvent(VirtualMachineHolograph vm, FieldMirror field) {
        this.vm = vm;
        this.field = field;
    }

    public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
        InstanceMirror target = (InstanceMirror)args.get(0);
        Object newValue = args.get(1);
        try {
            Reflection.setField(target, field, newValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
