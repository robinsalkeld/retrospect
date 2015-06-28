package edu.ubc.mirrors.holographs;

import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.AbstractMirrorEventRequest;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FieldMirrorSetRequest;

public class FieldHolographSetRequest extends AbstractMirrorEventRequest implements FieldMirrorSetRequest {

    private FieldMirror fieldFilter;
    private final List<String> classNamePatterns = new ArrayList<String>();
    
    public FieldHolographSetRequest(VirtualMachineHolograph vm, FieldMirror field) {
        super(vm);
        this.fieldFilter = field;
    }

    @Override
    public void addClassFilter(String classNamePattern) {
        classNamePatterns.add(classNamePattern);
    }

}
