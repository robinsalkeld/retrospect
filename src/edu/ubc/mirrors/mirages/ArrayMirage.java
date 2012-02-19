package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.ArrayMirror;

public class ArrayMirage extends ObjectMirage {
    protected final ArrayMirror mirror;
    
    public ArrayMirage(ArrayMirror mirror) {
        super(mirror);
        this.mirror = mirror;
    }

    public int length() {
        return mirror.length();
    }
}
