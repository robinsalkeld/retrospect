package edu.ubc.mirrors.raw.nativestubs.java.lang.reflect;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.nativestubs.java.lang.ClassLoaderStubs;

public class ProxyStubs extends NativeStubs {

    public ProxyStubs(ClassHolograph klass) {
	super(klass);
    }

    public Mirage defineClass0(Mirage loader, Mirage name, Mirage b, int off, int len) {
        String realClassName = Reflection.getRealStringForMirror((InstanceMirror)name.getMirror());
        
        if (loader == null) {
            return ObjectMirage.make(klass.getVM().defineBootstrapClass(realClassName, (ByteArrayMirror)b.getMirror(), off, len));
        } else {
            Mirage className = ObjectMirage.make(Reflection.makeString(klass.getVM(), realClassName));
            return ClassLoaderStubs.defineClass(loader, className, b, off, len, null, null);
        }
    }
}
