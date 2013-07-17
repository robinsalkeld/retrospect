package edu.ubc.mirrors.holograms;

import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.holograms.ArrayHologram;

public class CharArrayHologram extends ArrayHologram implements CharArrayMirror {

    protected final CharArrayMirror mirror;
    
    public CharArrayHologram(CharArrayMirror mirror) {
        super(mirror);
        this.mirror = mirror;
    }

    @Override
    public char getChar(int index) throws ArrayIndexOutOfBoundsException {
        return mirror.getChar(index);
    }

    @Override
    public void setChar(int index, char b) throws ArrayIndexOutOfBoundsException {
        mirror.setChar(index, b);
    }
    
    public static char getHologram(CharArrayMirror mirror, int index) throws Throwable {
        try {
            return mirror.getChar(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static void setHologram(CharArrayMirror mirror, int index, char b) throws Throwable {
        try {
            mirror.setChar(index, b);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        } catch (ArrayStoreException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
}
