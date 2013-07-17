package edu.ubc.mirrors.holograms;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

public class ObjectArrayHologram extends ObjectHologram implements ObjectArrayMirror {

    public final ObjectArrayMirror arrayMirror;
    
    public ObjectArrayHologram(ObjectArrayMirror mirror) {
        super(mirror);
        this.arrayMirror = mirror;
    }

    public ClassMirror getClassMirror() {
        return mirror.getClassMirror();
    }

    public int length() {
        return arrayMirror.length();
    }

    public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
        return arrayMirror.get(index);
    }

    public void set(int index, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        arrayMirror.set(index, o);
    }
    
    public static Hologram getHologram(ObjectArrayMirror mirror, int index) throws Throwable {
        try {
            return ObjectHologram.make(mirror.get(index));
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static void setHologram(ObjectArrayMirror mirror, int index, Hologram m) throws Throwable {
        try {
            mirror.set(index, ObjectHologram.getMirror(m));
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        } catch (ArrayStoreException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
}
