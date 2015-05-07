package edu.ubc.mirrors.holograms;

import java.util.List;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;

public class FieldSetProceed implements MirrorInvocationHandler {
    
    private final FieldMirror field;
    
    public FieldSetProceed(FieldMirror field) {
        this.field = field;
    }
    
    @Override
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
