package edu.ubc.mirrors.jdi;

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;

public class JDIInstanceMirror extends JDIObjectMirror implements InstanceMirror {

    public JDIInstanceMirror(JDIVirtualMachineMirror vm, ObjectReference t) {
        super(vm, t);
    }

    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        Field f = mirror.referenceType().fieldByName(name);
        if (f == null) {
            throw new NoSuchFieldException(name);
        }
        return new JDIMemberFieldMirror(vm, f, mirror);
    }

    @Override
    public List<FieldMirror> getMemberFields() {
        List<FieldMirror> result = new ArrayList<FieldMirror>();
        for (Field f : mirror.referenceType().allFields()) {
            result.add(new JDIMemberFieldMirror(vm, f, mirror));
        }
        return result;
    }

}
