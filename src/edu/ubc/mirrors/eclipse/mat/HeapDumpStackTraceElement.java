package edu.ubc.mirrors.eclipse.mat;

import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.fieldmap.DirectArrayMirror;
import edu.ubc.mirrors.fieldmap.FieldMapMirror;
import edu.ubc.mirrors.raw.NativeCharArrayMirror;
import edu.ubc.mirrors.raw.nativestubs.java.lang.SystemStubs;

public class HeapDumpStackTraceElement extends FieldMapMirror {

    private final ClassMirror stringClass;
    private final ClassMirror intClass;
    private final ClassMirror charArrayClass;
     
    public HeapDumpStackTraceElement(HeapDumpVirtualMachineMirror vm) {
        super(vm.findBootstrapClassMirror(StackTraceElement.class.getName()));
        this.stringClass = vm.findBootstrapClassMirror(String.class.getName());
        this.intClass = vm.getPrimitiveClass("int");
        this.charArrayClass = vm.getArrayClass(1, vm.getPrimitiveClass("char"));
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
        return s == null ? null : new HeapDumpString(s);
    }
    
    public class HeapDumpString extends FieldMapMirror {

        public HeapDumpString(String s) {
            super(stringClass);
            CharArrayMirror value = new DirectArrayMirror(charArrayClass, s.length());
            SystemStubs.arraycopyMirrors(new NativeCharArrayMirror(s.toCharArray()), 0, value, 0, s.length());
            try {
                getMemberField("value").set(value);
                getMemberField("count").setInt(s.length());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public FieldMirror getMemberField(String name) throws NoSuchFieldException {
            if (name.equals("value")) {
                return new MapEntryFieldMirror(name, charArrayClass);
            } else if (name.equals("offset") || name.equals("count") || name.equals("hash")) {
                return new MapEntryFieldMirror(name, intClass);
            } else {
                throw new NoSuchFieldException(name);
            }
        }
    }
}
