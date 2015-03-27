package edu.ubc.mirrors.jdi;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ThreadMirror;

public class JDIAnnotationMirror extends JDIMirror implements AnnotationMirror {

    private final ObjectReference mirror;
    private final InterfaceType annotType;
    
    public JDIAnnotationMirror(JDIVirtualMachineMirror vm, ThreadReference thread, ObjectReference mirror) {
        super(vm, mirror);
        this.mirror = mirror;
        
        ReferenceType annotationRT = vm.jdiVM.classesByName(Annotation.class.getName()).get(0);
        Method annotationTypeMethod = annotationRT.methodsByName("annotationType").get(0);
        
        ClassObjectReference annotTypeRef = (ClassObjectReference)vm.safeInvoke(mirror, thread, annotationTypeMethod);
        annotType = (InterfaceType)annotTypeRef.reflectedType();
    }

    @Override
    public ClassMirror getClassMirror() {
        return vm.makeClassMirror(annotType);
    }
    
    @Override
    public List<String> getKeys() {
        List<String> result = new ArrayList<String>();
        for (Method m : annotType.methods()) {
            result.add(m.name());
        }
        return result;
    }

    @Override
    public Object getValue(ThreadMirror thread, String name) {
        Method interfaceMethod = annotType.methodsByName(name).get(0);
        ThreadReference threadRef = ((JDIThreadMirror)thread).thread;
        return vm.wrapAnnotationValue(threadRef, vm.safeInvoke(mirror, threadRef, interfaceMethod));
    }
}
