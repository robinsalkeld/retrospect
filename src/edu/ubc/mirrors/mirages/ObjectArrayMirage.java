package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.ObjectMirage.throwableAsMirage;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.fieldmap.DirectArrayMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;

public class ObjectArrayMirage extends ObjectMirage implements ObjectArrayMirror {

    public final ObjectArrayMirror arrayMirror;
    
    public ObjectArrayMirage(ObjectArrayMirror mirror) {
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
    
    public static Mirage getMirage(ObjectArrayMirror mirror, int index) throws Throwable {
        try {
            return ObjectMirage.make(mirror.get(index));
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static void setMirage(ObjectArrayMirror mirror, int index, Mirage m) throws Throwable {
        try {
            mirror.set(index, ObjectMirage.getMirror(m));
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        } catch (ArrayStoreException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        }
    }
}
