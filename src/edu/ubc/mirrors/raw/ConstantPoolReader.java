package edu.ubc.mirrors.raw;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.ClassReader;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.mirages.Reflection;


public class ConstantPoolReader implements InstanceMirror {

    private final ClassMirror klass;
    private final ClassReader reader;
    private final char[] buf;
    
    private static final Method readUTFMethod;
    static {
        try {
            readUTFMethod = ClassReader.class.getDeclaredMethod("readUTF", Integer.TYPE, Integer.TYPE, char[].class);
            readUTFMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    
    public ConstantPoolReader(ClassMirror klass) {
        this.klass = klass;
        this.reader = new ClassReader(klass.getBytecode());
        this.buf = new char[reader.getMaxStringLength()];
    }
    
    public int getSize() {
        return reader.getItemCount();
    }

    public ClassMirror getClassAt(int index) {
        String name = reader.readClass(reader.getItem(index), buf);
        return HolographInternalUtils.loadClassMirrorInternal(klass, name);
    }
    
    public InstanceMirror getStringAt(int index) {
        int offset = reader.getItem(index);
        String s;
        try {
            s = (String)readUTFMethod.invoke(reader, offset + 2, reader.readUnsignedShort(offset), buf);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return Reflection.makeString(klass.getVM(), s);
    }
    
    @Override
    public ClassMirror getClassMirror() {
        return klass.getVM().findBootstrapClassMirror(Object.class.getName());
    }

    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        throw new NoSuchFieldException(name);
    }

    @Override
    public List<FieldMirror> getMemberFields() {
        return Collections.emptyList();
    }
}
