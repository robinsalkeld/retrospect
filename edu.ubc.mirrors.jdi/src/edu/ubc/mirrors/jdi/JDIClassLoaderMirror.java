package edu.ubc.mirrors.jdi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ReferenceType;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;

public class JDIClassLoaderMirror extends JDIInstanceMirror implements ClassMirrorLoader {

    private final Map<String, ReferenceType> loadedClasses = new HashMap<String, ReferenceType>();
    
    public JDIClassLoaderMirror(JDIVirtualMachineMirror vm, ClassLoaderReference loader) {
        super(vm, loader);
        for (ReferenceType klass : loader.definedClasses()) {
            loadedClasses.put(klass.name(), klass);
        }
    }

    @Override
    public ClassMirror findLoadedClassMirror(String name) {
        return vm.makeClassMirror(loadedClasses.get(name));
    }

    @Override
    public List<ClassMirror> loadedClassMirrors() {
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        for (ReferenceType klass : loadedClasses.values()) {
            result.add((ClassMirror)vm.makeMirror(klass.classObject()));
        }
        return result;
    }
    
    @Override
    public ClassMirror defineClass(String name, ByteArrayMirror b, int off,
            int len, InstanceMirror pd, InstanceMirror source, boolean unsafe) {

        throw new UnsupportedOperationException();
    }

}
