package edu.ubc.mirrors.holographs;

import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.AbstractMirrorEventRequest;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FieldMirrorSetHandlerRequest;
import edu.ubc.mirrors.Reflection;

public class FieldHolographSetHandlerRequest extends AbstractMirrorEventRequest implements FieldMirrorSetHandlerRequest {

    private final String declaringClass;
    private final String fieldName;
    private final List<String> classNamePatterns = new ArrayList<String>();
    
    public FieldHolographSetHandlerRequest(VirtualMachineHolograph vm, String declaringClass, String fieldName) {
        super(vm);
        this.declaringClass = declaringClass;
        this.fieldName = fieldName;
    }

    @Override
    public void addClassFilter(String classNamePattern) {
        classNamePatterns.add(classNamePattern);
    }
    
    public boolean matches(FieldMirror field) {
        if (!classNamePatterns.isEmpty()) {
            if (!classNamePatterns.contains(field.getDeclaringClass().getClassName())) {
                return false;
            }
        }
        
        return Reflection.fieldMatches(field, declaringClass, fieldName);
    }
}
