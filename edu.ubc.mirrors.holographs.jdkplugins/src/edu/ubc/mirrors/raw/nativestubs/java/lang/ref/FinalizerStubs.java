package edu.ubc.mirrors.raw.nativestubs.java.lang.ref;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class FinalizerStubs extends NativeStubs {

    public FinalizerStubs(ClassHolograph klass) {
        super(klass);
    }

    @StubMethod
    public void invokeFinalizeMethod(ObjectMirror o) throws Exception {
        ClassMirror objectMirror = getVM().findBootstrapClassMirror(Object.class.getName());
        MethodMirror finalizeMethod = objectMirror.getDeclaredMethod("finalize");
        finalizeMethod.invoke(ThreadHolograph.currentThreadMirror(), o);
    }
}
