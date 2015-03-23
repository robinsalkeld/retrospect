
package edu.ubc.mirrors;

import java.util.Enumeration;

public class EnumerationMirror implements Enumeration<ObjectMirror> {

    private final ThreadMirror thread;
    private final InstanceMirror enumMirror;
    private ClassMirror enumInterfaceMirror;
    private MethodMirror hasMoreElementsMethod;
    private MethodMirror nextElementMethod;

    public EnumerationMirror(ThreadMirror thread, InstanceMirror enumMirror) {
        this.thread = thread;
        this.enumMirror = enumMirror;
        this.enumInterfaceMirror = enumMirror.getClassMirror().getVM().findBootstrapClassMirror(Enumeration.class.getName());
    }
    
    @Override
    public boolean hasMoreElements() {
        if (hasMoreElementsMethod == null) {
            try {
                hasMoreElementsMethod = enumInterfaceMirror.getDeclaredMethod("hasMoreElements");
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        
        try {
            return (boolean) hasMoreElementsMethod.invoke(thread, enumMirror);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (MirrorInvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ObjectMirror nextElement() {
        if (nextElementMethod == null) {
            try {
                nextElementMethod = enumInterfaceMirror.getDeclaredMethod("nextElement");
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        
        try {
            return (ObjectMirror)nextElementMethod.invoke(thread, enumMirror);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (MirrorInvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
