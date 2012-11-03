package edu.ubc.mirrors.jdi;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;

public class JDIFieldMirror extends JDIMirror implements FieldMirror {

    final Field field;
    
    public JDIFieldMirror(JDIVirtualMachineMirror vm, Field field) {
	super(vm, field);
        this.field = field;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JDIFieldMirror)) {
            return false;
        }
        
        JDIFieldMirror other = (JDIFieldMirror)obj;
        return field.equals(other.field) && vm.equals(other.vm);
    }
    
    @Override
    public int hashCode() {
        return 11 * field.hashCode() * vm.hashCode();
    }
    
    @Override
    public ClassMirror getDeclaringClass() {
        return vm.makeClassMirror(field.declaringType());
    }
    
    @Override
    public String getName() {
        return field.name();
    }

    @Override
    public ClassMirror getType() {
        try {
            return vm.makeClassMirror(field.type());
        } catch (ClassNotLoadedException e) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public int getModifiers() {
        return field.modifiers();
    }
}
