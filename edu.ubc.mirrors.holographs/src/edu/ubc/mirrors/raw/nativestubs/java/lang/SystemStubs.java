package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.Reflection;

public class SystemStubs extends NativeStubs {

    public SystemStubs(ClassHolograph klass) {
	super(klass);
    }

    public int identityHashCode(Mirage o) {
        return o.getMirror().identityHashCode();
    }
    
    public void arraycopy(Mirage src, int srcPos, Mirage dest, int destPos, int length) {
        Reflection.arraycopy(src.getMirror(), srcPos, dest.getMirror(), destPos, length);
    }
    
    public void setIn0(Mirage stream) throws IllegalAccessException, NoSuchFieldException {
        ClassMirror systemClass = getVM().findBootstrapClassMirror(System.class.getName());
        systemClass.getStaticFieldValues().set(systemClass.getDeclaredField("in"), stream.getMirror());
    }
    
    public void setOut0(Mirage stream) throws IllegalAccessException, NoSuchFieldException {
        ClassMirror systemClass = getVM().findBootstrapClassMirror(System.class.getName());
        systemClass.getStaticFieldValues().set(systemClass.getDeclaredField("out"), stream.getMirror());
    }
    
    public void setErr0(Mirage stream) throws IllegalAccessException, NoSuchFieldException {
        ClassMirror systemClass = getVM().findBootstrapClassMirror(System.class.getName());
        systemClass.getStaticFieldValues().set(systemClass.getDeclaredField("err"), stream.getMirror());
    }
    
    // TODO-RS: I don't like this as a general rule, but it's called from 
    // ClassLoader#defineClass() in JDK 7 to measure loading time,
    // and also seems necessary in the read-only mapped fs.
    // It's probably actually okay, because this will just look like a very long
    // system delay to the original process, which is fairly reasonable.
    // That is, it's okay in type 1 time-travel programming, but not type 2!
    public long nanoTime() {
        return System.nanoTime();
    }
    
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
    
}
