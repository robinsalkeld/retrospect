package edu.ubc.mirrors.holographs.jdi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.mirages.MethodHandle;

public class HolographObjectReference extends Holograph implements ObjectReference {

    final ObjectReference wrapped;
    
    /**
     * @return
     * @see com.sun.jdi.Mirror#toString()
     */
    public String toString() {
        return wrapped.toString();
    }

    /**
     * @return
     * @see com.sun.jdi.Value#type()
     */
    public Type type() {
        return vm.wrapType(wrapped.type());
    }

    /**
     * 
     * @see com.sun.jdi.ObjectReference#disableCollection()
     */
    public void disableCollection() {
        wrapped.disableCollection();
    }

    /**
     * 
     * @see com.sun.jdi.ObjectReference#enableCollection()
     */
    public void enableCollection() {
        wrapped.enableCollection();
    }

    /**
     * @return
     * @throws IncompatibleThreadStateException
     * @see com.sun.jdi.ObjectReference#entryCount()
     */
    public int entryCount() throws IncompatibleThreadStateException {
        return wrapped.entryCount();
    }

    /**
     * @param arg1
     * @return
     * @see com.sun.jdi.ObjectReference#getValue(com.sun.jdi.Field)
     */
    public Value getValue(Field arg1) {
        HolographField field = (HolographField)arg1;
        HolographReferenceType declaringType = (HolographReferenceType)field.declaringType();
        ObjectReference objectMirror = getObjectMirror();
        if (field.name().equals("<mirror>")) {
            return objectMirror;
        }
        
        ObjectReference fieldMirror = (ObjectReference)vm.invokeMethodHandle(declaringType.getClassMirror(), new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((ClassMirror)null).getDeclaredField(null);
            }
        }, vm.mirrorOf(field.name()));
        ObjectReference fieldType = (ObjectReference)vm.invokeMethodHandle(fieldMirror, new MethodHandle() {
            protected void methodCall() {
                ((FieldMirror)null).getType();
            }
        });
        BooleanValue isPrimitive = (BooleanValue)vm.invokeMethodHandle(fieldType, new MethodHandle() {
            protected void methodCall() {
                ((ClassMirror)null).isPrimitive();
            }
        });
        Value value;
        if (isPrimitive.booleanValue()) {
            StringReference typeName = (StringReference)vm.invokeMethodHandle(fieldType, new MethodHandle() {
                protected void methodCall() {
                    ((ClassMirror)null).getClassName();
                }
            });
            ReferenceType targetClass = (ReferenceType)vm.wrappedVM.classesByName(FieldMirror.class.getName()).get(0);
            String primitiveTypeName = typeName.value();
            String capitalizedTypeName = primitiveTypeName.substring(0, 1).toUpperCase() + primitiveTypeName.substring(1);
            Method method = (Method)targetClass.methodsByName("get" + capitalizedTypeName).get(0);
            value = vm.invokeMethod(fieldMirror, method, objectMirror);
        } else {
            ObjectReference mirror = (ObjectReference)vm.invokeMethodHandle(objectMirror, new MethodHandle() {
                protected void methodCall() throws Throwable {
                    ((InstanceMirror)null).get(null);
                } 
            }, fieldMirror);
            value = vm.invokeMethodHandle(null, MethodHandle.OBJECT_MIRAGE_MAKE, mirror);
        }
        return vm.wrapValue(value);
    }

    public ObjectReference getObjectMirror() {
        return (ObjectReference)vm.invokeMethodHandle(wrapped, MethodHandle.OBJECT_MIRAGE_GET_MIRROR);
    }
    
    /**
     * @param arg1
     * @return
     * @see com.sun.jdi.ObjectReference#getValues(java.util.List)
     */
    public Map getValues(List arg1) {
        Map<Field, Value> result = new HashMap<Field, Value>();
        for (Object e : wrapped.getValues(arg1).entrySet()) {
            Map.Entry entry = (Map.Entry)e;
            Field f = (Field)entry.getKey();
            Value v = (Value)entry.getValue();
            result.put(f, vm.wrapValue(v));
        }
        return result;
    }

    /**
     * @param arg1
     * @param arg2
     * @param arg3
     * @param arg4
     * @return
     * @throws InvalidTypeException
     * @throws ClassNotLoadedException
     * @throws IncompatibleThreadStateException
     * @throws InvocationException
     * @see com.sun.jdi.ObjectReference#invokeMethod(com.sun.jdi.ThreadReference, com.sun.jdi.Method, java.util.List, int)
     */
    public Value invokeMethod(ThreadReference arg1, Method arg2, List arg3,
            int arg4) throws InvalidTypeException, ClassNotLoadedException,
            IncompatibleThreadStateException, InvocationException {
        ThreadReference unwrappedThread = vm.unwrapThread(arg1);
        List unwrappedArgs = vm.unwrapObjectReferences(arg3);
        return wrapped.invokeMethod(unwrappedThread, arg2, unwrappedArgs, arg4);
    }

    /**
     * @return
     * @see com.sun.jdi.ObjectReference#isCollected()
     */
    public boolean isCollected() {
        return wrapped.isCollected();
    }

    /**
     * @return
     * @throws IncompatibleThreadStateException
     * @see com.sun.jdi.ObjectReference#owningThread()
     */
    public ThreadReference owningThread()
            throws IncompatibleThreadStateException {
        return wrapped.owningThread();
    }

    /**
     * @return
     * @see com.sun.jdi.ObjectReference#referenceType()
     */
    public ReferenceType referenceType() {
        return vm.wrapReferenceType(wrapped.referenceType());
    }

    /**
     * @param arg1
     * @param arg2
     * @throws InvalidTypeException
     * @throws ClassNotLoadedException
     * @see com.sun.jdi.ObjectReference#setValue(com.sun.jdi.Field, com.sun.jdi.Value)
     */
    public void setValue(Field arg1, Value arg2) throws InvalidTypeException,
            ClassNotLoadedException {
        wrapped.setValue(arg1, arg2);
    }

    /**
     * @return
     * @see com.sun.jdi.ObjectReference#uniqueID()
     */
    public long uniqueID() {
        return wrapped.uniqueID();
    }

    /**
     * @return
     * @throws IncompatibleThreadStateException
     * @see com.sun.jdi.ObjectReference#waitingThreads()
     */
    public List waitingThreads() throws IncompatibleThreadStateException {
        return wrapped.waitingThreads();
    }

    /**
     * @param arg1
     * @return
     * @see com.sun.jdi.ObjectReference#referringObjects(long)
     */
    public List referringObjects(long arg1) {
        return wrapped.referringObjects(arg1);
    }

    public HolographObjectReference(JDIHolographVirtualMachine vm, ObjectReference wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

}
