package edu.ubc.mirrors.mutable;

import java.util.HashMap;
import java.util.Map;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;

public class MutableInstanceMirror implements InstanceMirror {

    private final MutableClassMirrorLoader loader;
    private final Map<String, FieldMirror> fields = new HashMap<String, FieldMirror>();
    private final InstanceMirror immutableMirror;
    private final InstanceMirror mutableLayer;
    
    @Override
    public ClassMirror getClassMirror() {
        return (ClassMirror)loader.makeMirror(immutableMirror.getClassMirror());
    }

    public MutableInstanceMirror(MutableClassMirrorLoader loader, InstanceMirror immutableMirror) {
        this.loader = loader;
        this.immutableMirror = immutableMirror;
        this.mutableLayer = new FieldMapMirror(null);
    }
    
    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        FieldMirror result = fields.get(name);
        if (result == null) {
            result = new MutableFieldMirror(loader, mutableLayer.getMemberField(name), immutableMirror.getMemberField(name));
            fields.put(name, result);
        }
        return result;
    }
}
