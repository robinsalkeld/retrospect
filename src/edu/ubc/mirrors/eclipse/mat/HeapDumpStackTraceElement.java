package edu.ubc.mirrors.eclipse.mat;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;
import edu.ubc.mirrors.fieldmap.FieldMapStringMirror;

public class HeapDumpStackTraceElement extends FieldMapMirror {

    private final VirtualMachineMirror vm;
    
    private final ClassMirror stringClass;
    private final ClassMirror intClass;
     
    public HeapDumpStackTraceElement(HeapDumpVirtualMachineMirror vm) {
        super(vm.findBootstrapClassMirror(StackTraceElement.class.getName()));
        this.vm = vm;
        this.stringClass = vm.findBootstrapClassMirror(String.class.getName());
        this.intClass = vm.getPrimitiveClass("int");
    }
    
    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        if (name.equals("declaringClass") || name.equals("methodName") || name.equals("fileName")) {
            return new MapEntryFieldMirror(name, stringClass);
        } else if (name.equals("lineNumber")) {
            return new MapEntryFieldMirror(name, intClass);
        } else {
            throw new NoSuchFieldException(name);
        }
    }

    public InstanceMirror makeString(String s) {
        return s == null ? null : new FieldMapStringMirror(vm, s);
    }
}
