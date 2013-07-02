package edu.ubc.mirrors.asjdi;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.asjdi.MirrorsMethod;
import edu.ubc.mirrors.asjdi.MirrorsMirror;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsObjectReference extends MirrorsMirror implements ObjectReference {

    protected final ObjectMirror wrapped;
    private final long uniqueID;
    private static long nextUniqueID = 0;
    
    public MirrorsObjectReference(MirrorsVirtualMachine vm, ObjectMirror wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
        this.uniqueID = nextUniqueID++;
    }

    @Override
    public Type type() {
        return referenceType();
    }

    @Override
    public void disableCollection() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enableCollection() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int entryCount() throws IncompatibleThreadStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value getValue(Field field) {
        if (wrapped instanceof InstanceMirror) {
            return vm.getValue((InstanceMirror)wrapped, field);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Map<Field, Value> getValues(List fields) {
        Map<Field, Value> result = new HashMap<Field, Value>(fields.size());
        for (Object field : fields) {
            result.put((Field)field, getValue((Field)field));
        }
        return result;
    }

    @Override
    public Value invokeMethod(ThreadReference thread, Method method,
            List args, int options) throws InvalidTypeException,
            ClassNotLoadedException, IncompatibleThreadStateException,
            InvocationException {
        
        return ((MirrorsMethod)method).invoke(thread, wrapped, args, options);
    }

    @Override
    public boolean isCollected() {
        return false;
    }

    @Override
    public ThreadReference owningThread() throws IncompatibleThreadStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReferenceType referenceType() {
        return (ReferenceType)vm.typeForClassMirror(wrapped.getClassMirror());
    }

    @Override
    public List<ObjectReference> referringObjects(long arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(Field field, Value value) throws InvalidTypeException, ClassNotLoadedException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public long uniqueID() {
        return uniqueID;
    }

    @Override
    public List<ThreadReference> waitingThreads() throws IncompatibleThreadStateException {
        return Collections.emptyList();
    }

}
