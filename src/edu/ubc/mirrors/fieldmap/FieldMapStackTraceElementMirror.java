package edu.ubc.mirrors.fieldmap;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.mirages.Reflection;

public class FieldMapStackTraceElementMirror extends FieldMapMirror {

    private final ClassMirror stringClass;
    private final ClassMirror intClass;
     
    public FieldMapStackTraceElementMirror(VirtualMachineMirror vm, String declaringClass, String methodName, String fileName, int line) {
        super(vm.findBootstrapClassMirror(StackTraceElement.class.getName()));
        this.stringClass = vm.findBootstrapClassMirror(String.class.getName());
        this.intClass = vm.getPrimitiveClass("int");
        
        try {
            getMemberField("declaringClass").set(Reflection.makeString(vm, declaringClass));
            getMemberField("methodName").set(Reflection.makeString(vm, methodName));
            getMemberField("fileName").set(Reflection.makeString(vm, fileName));
            getMemberField("lineNumber").setInt(line);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
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
}
