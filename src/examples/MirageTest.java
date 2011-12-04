package examples;

import edu.ubc.mirrors.NativeObjectMirror;
import edu.ubc.mirrors.ObjectMirage;
import edu.ubc.mirrors.ObjectMirror;

public class MirageTest {

    public static void main(String[] args) throws Exception {
        Bar bar = new Bar();
        Class<?> fooMirageClass = ObjectMirage.getMirageClass(Bar.class);
        ObjectMirror<Bar> fooMirror = new NativeObjectMirror<Bar>(bar);
        Object barInstance = fooMirageClass.getConstructor(ObjectMirror.class).newInstance(fooMirror);
        fooMirageClass.getMethod("bar").invoke(barInstance);
    }
    
}
