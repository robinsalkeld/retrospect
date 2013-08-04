package edu.ubc.mirrors.raw.nativestubs.java.lang.reflect;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;
import edu.ubc.mirrors.raw.nativestubs.java.lang.ClassLoaderStubs;

public class ProxyStubs extends NativeStubs {

    public ProxyStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public ClassMirror defineClass0(ClassMirrorLoader loader, InstanceMirror name, ByteArrayMirror b, int off, int len) {
        String realClassName = Reflection.getRealStringForMirror(name);
        
        if (loader == null) {
            return klass.getVM().defineBootstrapClass(realClassName, b, off, len);
        } else {
            InstanceMirror className = Reflection.makeString(klass.getVM(), realClassName);
            return ClassLoaderStubs.defineClass(loader, className, b, off, len, null, null);
        }
    }
}
