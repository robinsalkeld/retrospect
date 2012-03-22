package edu.ubc.mirrors.sunhat;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mat.snapshot.model.Field;

import com.sun.tools.hat.internal.model.JavaObject;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpFieldMirror;
import edu.ubc.mirrors.sunhat.JHatClassMirror;
import edu.ubc.mirrors.sunhat.JHatClassMirrorLoader;
import edu.ubc.mirrors.sunhat.JHatFieldMirror;

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
    
    @Override
    public List<FieldMirror> getMemberFields() {
        // TODO-RS: Implement
        return null;
    }
}
