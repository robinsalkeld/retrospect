package edu.ubc.mirrors.holographs;

import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.AbstractMirrorEventRequest;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FieldMirrorGetHandlerRequest;
import edu.ubc.mirrors.Reflection;

public class FieldHolographGetHandlerRequest extends AbstractMirrorEventRequest implements FieldMirrorGetHandlerRequest {

    private final String declaringClass;
    private final String fieldName;
    private final List<String> classNamePatterns = new ArrayList<String>();
    
    public FieldHolographGetHandlerRequest(VirtualMachineHolograph vm, String declaringClass, String name) {
        super(vm);
        this.declaringClass = declaringClass;
        this.fieldName = name;
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
