package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.mirages.Reflection;

public class SystemStubs extends NativeStubs {

    public SystemStubs(ClassHolograph klass) {
	super(klass);
    }

    public int identityHashCode(ObjectMirror o) {
        return o.identityHashCode();
    }
    
    public void arraycopy(ObjectMirror src, int srcPos, ObjectMirror dest, int destPos, int length) {
        Reflection.arraycopy(src, srcPos, dest, destPos, length);
    }
    
    public void setIn0(InstanceMirror stream) throws IllegalAccessException, NoSuchFieldException {
        ClassMirror systemClass = getVM().findBootstrapClassMirror(System.class.getName());
        systemClass.getStaticFieldValues().set(systemClass.getDeclaredField("in"), stream);
    }
    
    public void setOut0(InstanceMirror stream) throws IllegalAccessException, NoSuchFieldException {
        ClassMirror systemClass = getVM().findBootstrapClassMirror(System.class.getName());
        systemClass.getStaticFieldValues().set(systemClass.getDeclaredField("out"), stream);
    }
    
    public void setErr0(InstanceMirror stream) throws IllegalAccessException, NoSuchFieldException {
        ClassMirror systemClass = getVM().findBootstrapClassMirror(System.class.getName());
        systemClass.getStaticFieldValues().set(systemClass.getDeclaredField("err"), stream);
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
