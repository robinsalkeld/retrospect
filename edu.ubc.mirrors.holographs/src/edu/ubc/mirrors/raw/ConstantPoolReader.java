package edu.ubc.mirrors.raw;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassReader;

import edu.ubc.mirrors.BlankInstanceMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holographs.HolographInternalUtils;


public class ConstantPoolReader extends BlankInstanceMirror implements InstanceMirror {

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
}
