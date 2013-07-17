package edu.ubc.mirrors.holograms;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.holograms.ObjectHologram;

public class ArrayHologram extends ObjectHologram {
    protected final ArrayMirror mirror;
    
    public ArrayHologram(ArrayMirror mirror) {
        super(mirror);
        this.mirror = mirror;
    }

    public int length() {
        return mirror.length();
    }
}
