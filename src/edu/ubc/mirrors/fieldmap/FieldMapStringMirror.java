package edu.ubc.mirrors.fieldmap;

import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.raw.NativeCharArrayMirror;
import edu.ubc.mirrors.raw.nativestubs.java.lang.SystemStubs;

public class FieldMapStringMirror extends FieldMapMirror {

    private final String s;
    private final ClassMirror intClass;
    private final ClassMirror charArrayClass;
    
    public FieldMapStringMirror(VirtualMachineMirror vm, String s) {
        super(vm.findBootstrapClassMirror(String.class.getName()));
        this.s = s;
        this.intClass = vm.getPrimitiveClass("int");
        this.charArrayClass = vm.getArrayClass(1, vm.getPrimitiveClass("char"));
        
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
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + s + "]";
    }
}