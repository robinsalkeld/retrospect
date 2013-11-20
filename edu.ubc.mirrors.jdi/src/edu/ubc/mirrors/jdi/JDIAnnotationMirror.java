package edu.ubc.mirrors.jdi;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ClassMirror;

public class JDIAnnotationMirror extends JDIMirror implements AnnotationMirror {

    private final ObjectReference mirror;
    private final InterfaceType annotType;
    
    public JDIAnnotationMirror(JDIVirtualMachineMirror vm, ObjectReference mirror) {
        super(vm, mirror);
        this.mirror = mirror;
        
        ReferenceType annotationRT = vm.jdiVM.classesByName(Annotation.class.getName()).get(0);
        Method annotationTypeMethod = annotationRT.methodsByName("annotationType").get(0);
        
        ClassObjectReference annotTypeRef = (ClassObjectReference)JDIVirtualMachineMirror.safeInvoke(mirror, vm.invokeThread, annotationTypeMethod);
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
    public Object getValue(String name) {
        Method interfaceMethod = annotType.methodsByName(name).get(0);
        return vm.wrapAnnotationValue(JDIVirtualMachineMirror.safeInvoke(mirror, vm.invokeThread, interfaceMethod));
    }
}
