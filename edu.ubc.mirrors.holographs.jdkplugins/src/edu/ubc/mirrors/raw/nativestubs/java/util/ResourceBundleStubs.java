package edu.ubc.mirrors.raw.nativestubs.java.util;

import java.util.List;

import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class ResourceBundleStubs extends NativeStubs {

    public ResourceBundleStubs(ClassHolograph klass) {
        super(klass);
    }

    @StubMethod
    public ObjectArrayMirror getClassContext() {
        List<FrameMirror> frames = ThreadHolograph.currentThreadMirror().getStackTrace();
        
        ObjectArrayMirror result = (ObjectArrayMirror)getVM().findBootstrapClassMirror(Class.class.getName()).newArray(frames.size());
        for (int i = 0; i < result.length(); i++) {
            result.set(i, frames.get(i).declaringClass());
        }
        return result;
    }
    
}
