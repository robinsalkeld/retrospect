package examples;

import edu.ubc.mirrors.FieldMapMirror;
import edu.ubc.mirrors.NativeObjectMirror;
import edu.ubc.mirrors.ObjectMirage;
import edu.ubc.mirrors.ObjectMirror;

public class MirageTest {

    public static void main(String[] args) throws Exception {
        ObjectMirror<Bar> fooMirror = new FieldMapMirror<Bar>(Bar.class);
        System.out.println(Bar.class.getClassLoader());
        Bar barMirage = ObjectMirage.make(fooMirror);
        barMirage.bar();
    }
    
}
