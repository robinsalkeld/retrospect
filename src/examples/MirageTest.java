package examples;

import edu.ubc.mirrors.FieldMapMirror;
import edu.ubc.mirrors.ObjectMirage;
import edu.ubc.mirrors.ObjectMirror;


public class MirageTest extends MirageTestSuper {

    String c;
    
    public MirageTest(String c) {
        this.c = c;
    }
    
    public static void main(String[] args) throws Exception {
        Bar bar = new Bar(12);
        bar.bar(42);
        
        FieldMapMirror<Bar> mirror = new FieldMapMirror<Bar>(Bar.class);
        Bar b = ObjectMirage.make(mirror);
        b.bar(9);
        
        
    }
    
}
