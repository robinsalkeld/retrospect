package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.InstanceHolograph;
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
    public void gc(InstanceMirror runtime) throws Exception {
        Runtime.getRuntime().gc();
        
        // TODO-RS: This should be happening on a background ReferenceHandler thread
        // as well.
        InstanceHolograph.enqueuePhantomReferences(ThreadHolograph.currentThreadMirror());
    }
    
    @StubMethod
    public void runFinalization0() throws Exception {
        ClassMirror finalizerClass = getVM().findBootstrapClassMirror("java.lang.ref.Finalizer");
        MethodMirror finalizationMethod = finalizerClass.getDeclaredMethod("runFinalization");
        finalizationMethod.invoke(ThreadHolograph.currentThreadMirror(), null);
    }
}
