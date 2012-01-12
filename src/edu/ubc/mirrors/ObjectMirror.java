package edu.ubc.mirrors;

public interface ObjectMirror<T> {
    
    // TODO: Refactor into ClassMirror interface instead
    public FieldMirror getMemberField(String name) throws NoSuchFieldException;
    public FieldMirror getStaticField(String name) throws NoSuchFieldException;
    // TODO: Refactor completely - this is awful :)
    public FieldMirror getArrayElement(int index) throws ArrayIndexOutOfBoundsException;
    
    public Class<? extends T> getClassMirror();
}
