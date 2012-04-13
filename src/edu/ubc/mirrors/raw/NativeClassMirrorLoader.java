package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;

public class NativeClassMirrorLoader extends NativeObjectMirror implements ClassMirrorLoader {

    public final ClassLoader classLoader;
    
    public NativeClassMirrorLoader(ClassLoader classLoader) {
        super(classLoader);
        this.classLoader = classLoader;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NativeClassMirrorLoader)) {
            return false;
        }
        
        NativeClassMirrorLoader other = (NativeClassMirrorLoader)obj;
        return classLoader == null ? other.classLoader == null : classLoader.equals(other.classLoader);
    }
    
    @Override
    public int hashCode() {
        return 17 + classLoader.hashCode();
    }
    
    @Override
    public ClassMirror findLoadedClassMirror(String name) {
        Class<?> klass;
        try {
            // TODO-RS: Do we need to call the native findLoadedClass0() method directly?
            // Is this shortcut harmful?
            klass = Class.forName(name, false, classLoader);
            return new NativeClassMirror(klass);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
