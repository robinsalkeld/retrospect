package edu.ubc.mirrors.holograms;

import java.util.List;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;

public class FieldGetProceed implements MirrorInvocationHandler {
    
    private final FieldMirror field;
    
    public FieldGetProceed(FieldMirror field) {
        this.field = field;
    }
    
    @Override
    public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
        InstanceMirror target = (InstanceMirror)args.get(0);
        try {
            return Reflection.getFieldValue(target, field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
