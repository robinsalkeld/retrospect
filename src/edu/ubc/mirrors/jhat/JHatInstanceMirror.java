package edu.ubc.mirrors.jhat;

import com.sun.tools.hat.internal.model.JavaObject;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;

public class JHatInstanceMirror implements InstanceMirror {

    private final JHatClassMirrorLoader loader;
    
    private final JavaObject javaObject;
    
    public JHatInstanceMirror(JHatClassMirrorLoader loader, JavaObject javaObject) {
        this.loader = loader;
        this.javaObject = javaObject;
    }
    
    @Override
    public ClassMirror getClassMirror() {
        return new JHatClassMirror(loader, javaObject.getClazz());
    }

    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        return new JHatFieldMirror(loader, name, javaObject.getField(name));
    }
}
