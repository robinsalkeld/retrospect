package edu.ubc.mirrors;

public abstract class BlankInstanceMirror extends BoxingInstanceMirror {

    @Override
    public Object getBoxedValue(FieldMirror field) throws IllegalAccessException {
        return null;
    }

    @Override
    public void setBoxedValue(FieldMirror field, Object o) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int identityHashCode() {
        return hashCode();
    }
}
