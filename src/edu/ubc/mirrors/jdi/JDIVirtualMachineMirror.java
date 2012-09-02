package edu.ubc.mirrors.jdi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Mirror;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class JDIVirtualMachineMirror implements VirtualMachineMirror {

    private final VirtualMachine vm;

    private final Map<Mirror, ObjectMirror> mirrors = new HashMap<Mirror, ObjectMirror>();
    
    public JDIVirtualMachineMirror(VirtualMachine vm) {
        this.vm = vm;
    }
    
    @Override
    public ClassMirror findBootstrapClassMirror(String name) {
        for (ReferenceType t : vm.classesByName(name)) {
            if (t.classLoader() == null) {
                return makeClassMirror(t.classObject());
            }
        }
        return null;
    }

    @Override
    public List<ClassMirror> findAllClasses(String name, boolean includeSubclasses) {
        List<ClassMirror> classes = new ArrayList<ClassMirror>();
        for (ReferenceType t : vm.classesByName(name)) {
            classes.add(((ClassMirror)makeMirror(t.classObject())));
        }
        return classes;
    }

    @Override
    public List<ThreadMirror> getThreads() {
        List<ThreadMirror> threads = new ArrayList<ThreadMirror>();
        for (ThreadReference t : vm.allThreads()) {
            threads.add(((ThreadMirror)makeMirror(t)));
        }
        return threads;
    }

    public ObjectMirror makeMirror(Mirror t) {
        if (t == null) {
            return null;
        }
        
        ObjectMirror result = mirrors.get(t);
        if (result != null) {
            return result;
        }
        
        // TODO: Type subclasses, primitive arrays, 
        if (t instanceof ClassObjectReference) {
            result = new JDIClassMirror(this, (ClassObjectReference)t);
        } else if (t instanceof ClassLoaderReference) {
            result = new JDIClassLoaderMirror(this, (ClassLoaderReference)t);
        } else if (t instanceof ObjectReference) {
            result = new JDIObjectMirror(this, (ObjectReference)t);
        } else {
            throw new IllegalArgumentException();
        }
        
        mirrors.put(t, result);
        
        return result;
    }

    public ClassMirror makeClassMirror(Mirror r) {
        return (ClassMirror)makeMirror(r);
    }
    
    public List<ClassMirror> makeClassMirrorList(List<? extends ReferenceType> refTypes) {
        List<ClassMirror> result = new ArrayList<ClassMirror>(refTypes.size());
        for (ReferenceType refType : refTypes) {
            result.add(makeClassMirror(refType.classObject()));
        }
        return result;
    }
    
    @Override
    public ClassMirror getPrimitiveClass(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ClassMirror getArrayClass(int dimensions, ClassMirror elementClass) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
