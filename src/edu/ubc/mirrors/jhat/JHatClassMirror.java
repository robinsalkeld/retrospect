package edu.ubc.mirrors.jhat;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.tools.hat.internal.model.JavaClass;
import com.sun.tools.hat.internal.model.JavaObject;
import com.sun.tools.hat.internal.model.JavaObjectArray;
import com.sun.tools.hat.internal.model.JavaThing;
import com.sun.tools.hat.internal.model.JavaValueArray;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.ObjectMirror;
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
    
    public List<ObjectMirror> getInstances() {
        List<ObjectMirror> result = new ArrayList<ObjectMirror>();
        Enumeration<?> e = javaClass.getInstances(true);
        while (e.hasMoreElements()) {
            result.add(loader.getMirror((JavaThing)e.nextElement()));
        }
        return result;
    }

}
