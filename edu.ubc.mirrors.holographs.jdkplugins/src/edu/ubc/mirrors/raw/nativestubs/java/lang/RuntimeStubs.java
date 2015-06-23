package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class RuntimeStubs extends NativeStubs {

    public RuntimeStubs(ClassHolograph klass) {
        super(klass);
    }

    @StubMethod
    public long freeMemory(InstanceMirror runtime) {
        return Runtime.getRuntime().freeMemory();
    }
    
    @StubMethod
    public void gc(InstanceMirror runtime) {
        Runtime.getRuntime().gc();
    }
    
    @StubMethod
    public void runFinalization0() throws Exception {
        ClassMirror finalizerClass = getVM().findBootstrapClassMirror("java.lang.ref.Finalizer");
        MethodMirror finalizationMethod = finalizerClass.getDeclaredMethod("runFinalization");
        finalizationMethod.invoke(ThreadHolograph.currentThreadMirror(), null);
    }
}
