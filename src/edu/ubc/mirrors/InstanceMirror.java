package edu.ubc.mirrors;

import java.util.List;

public interface InstanceMirror extends ObjectMirror {

    // TODO: Refactor into ClassMirror interface instead
    public FieldMirror getMemberField(String name) throws NoSuchFieldException;
    
    public List<FieldMirror> getMemberFields();
}
