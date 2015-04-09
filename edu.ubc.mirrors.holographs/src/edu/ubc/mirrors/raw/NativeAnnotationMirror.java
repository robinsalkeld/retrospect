package edu.ubc.mirrors.raw;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ThreadMirror;

public class NativeAnnotationMirror implements AnnotationMirror {

    private Annotation annotation;
    
    public NativeAnnotationMirror(Annotation annotation) {
        this.annotation = annotation;
    }

    @Override
    public ClassMirror getClassMirror() {
        return new NativeClassMirror(annotation.getClass());
    }

    @Override
    public List<String> getKeys() {
        List<String> result = new ArrayList<String>();
        for (Method m : annotation.getClass().getMethods()) {
            result.add(m.getName());
        }
        return result;
    }

    @Override
    public Object getValue(ThreadMirror thread, String name) {
        try {
            Method method = annotation.getClass().getMethod(name);
            return method.invoke(annotation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
