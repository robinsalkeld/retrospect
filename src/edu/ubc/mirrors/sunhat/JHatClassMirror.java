package edu.ubc.mirrors.sunhat;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.sun.tools.hat.internal.model.JavaClass;
import com.sun.tools.hat.internal.model.JavaThing;

import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.sunhat.JHatClassMirrorLoader;
import edu.ubc.mirrors.sunhat.JHatFieldMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;

public class JHatClassMirror extends NativeClassMirror {

    private final JHatClassMirrorLoader loader;
    private final JavaClass javaClass;
    
    public JHatClassMirror(JHatClassMirrorLoader loader, JavaClass javaClass) {
        super(loader.getClassLoader(), javaClass.getName());
        this.loader = loader;
        this.javaClass = javaClass;
    }

    @Override
    public ClassMirrorLoader getLoader() {
        return loader;
    }

    @Override
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        return new JHatFieldMirror(loader, name, javaClass.getStaticField(name));
    }
    
    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        // The JHat model doesn't expose member fields for classes, so grab them
        // from the native super implementation.
        return super.getMemberField(name);
    }
    
    public List<InstanceMirror> getInstances() {
        List<InstanceMirror> result = new ArrayList<InstanceMirror>();
        Enumeration<?> e = javaClass.getInstances(true);
        while (e.hasMoreElements()) {
            result.add((InstanceMirror)loader.getMirror((JavaThing)e.nextElement()));
        }
        return result;
    }

}
