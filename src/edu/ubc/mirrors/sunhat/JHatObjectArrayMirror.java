package edu.ubc.mirrors.sunhat;

import com.sun.tools.hat.internal.model.JavaObjectArray;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.sunhat.JHatClassMirror;
import edu.ubc.mirrors.sunhat.JHatClassMirrorLoader;

public class JHatObjectArrayMirror implements ObjectArrayMirror {

    private final JavaObjectArray javaObjectArray;
    private final JHatClassMirrorLoader loader;
    
    public JHatObjectArrayMirror(JHatClassMirrorLoader loader, JavaObjectArray javaObjectArray) {
        this.loader = loader;
        this.javaObjectArray = javaObjectArray;
    }
    
    @Override
    public int length() {
        return javaObjectArray.getLength();
    }

    @Override
    public ClassMirror getClassMirror() {
        return new JHatClassMirror(loader, javaObjectArray.getClazz());
    }

    @Override
    public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
        return loader.getMirror(javaObjectArray.getElements()[index]);
    }

    @Override
    public void set(int index, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }
    
}
