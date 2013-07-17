package edu.ubc.mirrors.asjdi;

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.asjdi.MirrorsInstanceReference;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsThreadGroupReference extends MirrorsInstanceReference implements ThreadGroupReference {

    private final ClassMirror threadGroupClass;
    
    public MirrorsThreadGroupReference(MirrorsVirtualMachine vm, InstanceMirror wrapped) {
        super(vm, wrapped);
        this.threadGroupClass = vm.vm.findBootstrapClassMirror(ThreadGroup.class.getName());
    }

    @Override
    public String name() {
        return Reflection.getRealStringForMirror(readField(threadGroupClass, "name"));
    }

    @Override
    public ThreadGroupReference parent() {
        return (ThreadGroupReference)vm.wrapMirror(readField(threadGroupClass, "parent"));
    }

    @Override
    public void resume() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void suspend() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ThreadGroupReference> threadGroups() {
        List<ThreadGroupReference> result = new ArrayList<ThreadGroupReference>();
        for (ObjectMirror group : Reflection.fromArray((ObjectArrayMirror)readField(threadGroupClass, "groups"))) {
            result.add((ThreadGroupReference)vm.wrapMirror(group));
        }
        return result;
    }

    @Override
    public List<ThreadReference> threads() {
        List<ThreadReference> result = new ArrayList<ThreadReference>();
        for (ObjectMirror group : Reflection.fromArray((ObjectArrayMirror)readField(threadGroupClass, "threads"))) {
            result.add((ThreadReference)vm.wrapMirror(group));
        }
        return result;
    }
}
