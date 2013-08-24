package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class SystemStubs extends NativeStubs {

    public SystemStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public int identityHashCode(ObjectMirror o) {
        return o.identityHashCode();
    }
    
    @StubMethod
    public void arraycopy(ObjectMirror src, int srcPos, ObjectMirror dest, int destPos, int length) {
        Reflection.arraycopy(src, srcPos, dest, destPos, length);
    }
    
    @StubMethod
    public void setIn0(InstanceMirror stream) throws IllegalAccessException, NoSuchFieldException {
        ClassMirror systemClass = getVM().findBootstrapClassMirror(System.class.getName());
        systemClass.getStaticFieldValues().set(systemClass.getDeclaredField("in"), stream);
    }
    
    @StubMethod
    public void setOut0(InstanceMirror stream) throws IllegalAccessException, NoSuchFieldException {
        ClassMirror systemClass = getVM().findBootstrapClassMirror(System.class.getName());
        systemClass.getStaticFieldValues().set(systemClass.getDeclaredField("out"), stream);
    }
    
    @StubMethod
    public void setErr0(InstanceMirror stream) throws IllegalAccessException, NoSuchFieldException {
        ClassMirror systemClass = getVM().findBootstrapClassMirror(System.class.getName());
        systemClass.getStaticFieldValues().set(systemClass.getDeclaredField("err"), stream);
    }
    
    // TODO-RS: I don't like this as a general rule, but it's called from 
    // ClassLoader#defineClass() in JDK 7 to measure loading time,
    // and also seems necessary in the read-only mapped fs.
    // It's probably actually okay, because this will just look like a very long
    // system delay to the original process, which is fairly reasonable.
    @StubMethod
    public long nanoTime() {
        return System.nanoTime();
    }
    
    @StubMethod
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
    
    @StubMethod
    public InstanceMirror mapLibraryName(InstanceMirror libnameMirror) {
        // TODO-RS: Switch based on supported platforms.
        String libname = Reflection.getRealStringForMirror(libnameMirror);
        String mappedLibname = libname + ".so";
        return Reflection.makeString(getVM(), mappedLibname);
    }
}
