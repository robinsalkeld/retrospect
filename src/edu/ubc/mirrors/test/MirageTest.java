package edu.ubc.mirrors.test;

import edu.ubc.mirrors.fieldmap.FieldMapMirror;
import edu.ubc.mirrors.mirages.ObjectMirage;


public class MirageTest {

    String c;
    
    public MirageTest(String c) {
        this.c = c;
    }
    
    public static void main(String[] args) throws Exception {
        Bar bar = new Bar(12);
        bar.bar(42);
        
        FieldMapMirror mirror = new FieldMapMirror(Bar.class);
        Bar b = (Bar)ObjectMirage.make(mirror);
        b.bar(9);
        
        
    }
    
}
