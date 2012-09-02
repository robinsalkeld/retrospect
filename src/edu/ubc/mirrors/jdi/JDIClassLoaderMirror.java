package edu.ubc.mirrors.jdi;

import java.util.HashMap;
import java.util.Map;

import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ReferenceType;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;

public class JDIClassLoaderMirror extends JDIInstanceMirror implements ClassMirrorLoader {

    private final ClassLoaderReference loader;
    private final Map<String, ReferenceType> loadedClasses = new HashMap<String, ReferenceType>();
    
    public JDIClassLoaderMirror(JDIVirtualMachineMirror vm, ClassLoaderReference loader) {
        super(vm, loader);
        this.loader = loader;
        for (ReferenceType klass : loader.definedClasses()) {
            loadedClasses.put(klass.name(), klass);
        }
    }

    @Override
    public ClassMirror findLoadedClassMirror(String name) {
        return (ClassMirror)vm.makeMirror(loadedClasses.get(name));
    }

    @Override
    public ClassMirror defineClass1(String name, ByteArrayMirror b, int off,
            int len, InstanceMirror pd, InstanceMirror source) {

        throw new UnsupportedOperationException();
    }

}
