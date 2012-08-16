package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.CharArrayMirror;

public class CharArrayMirage extends ArrayMirage implements CharArrayMirror {

    protected final CharArrayMirror mirror;
    
    public CharArrayMirage(CharArrayMirror mirror) {
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
    
    public static char getMirage(CharArrayMirror mirror, int index) throws Throwable {
        try {
            return mirror.getChar(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static void setMirage(CharArrayMirror mirror, int index, char b) throws Throwable {
        try {
            mirror.setChar(index, b);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        } catch (ArrayStoreException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        }
    }
}
