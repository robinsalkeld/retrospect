package edu.ubc.mirrors.eclipse.mat;

import java.util.Collections;
import java.util.Set;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.IPathsFromGCRootsComputer;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.FieldDescriptor;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;
import org.eclipse.mat.snapshot.model.NamedReference;
import org.eclipse.mat.snapshot.model.ObjectReference;

import edu.ubc.mirrors.BoxingFieldMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.raw.NativeInstanceMirror;

public class HeapDumpFieldMirror extends BoxingFieldMirror {

    private final HeapDumpClassMirror declaringClass;
    final FieldDescriptor fieldDescriptor;
    
    private final HeapDumpVirtualMachineMirror vm;
    
    public HeapDumpFieldMirror(HeapDumpVirtualMachineMirror vm, HeapDumpClassMirror declaringClass, FieldDescriptor fieldDescriptor) {
        this.vm = vm;
        this.declaringClass = declaringClass;
        this.fieldDescriptor = fieldDescriptor;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HeapDumpFieldMirror)) {
            return false;
        }
        
        HeapDumpFieldMirror other = (HeapDumpFieldMirror)obj;
        return declaringClass.equals(other.declaringClass) && fieldDescriptor.equals(other.fieldDescriptor);
    }
    
    @Override
    public int hashCode() {
        return 7 * declaringClass.hashCode() * fieldDescriptor.hashCode();
    }
    
    @Override
    public ClassMirror getDeclaringClass() {
        return declaringClass;
    }
    
    @Override
    public Object getBoxedValue(InstanceMirror obj) throws IllegalAccessException {
        if (fieldDescriptor instanceof Field) {
            // Static field
            return ((Field)fieldDescriptor).getValue();
        } else {
            if (obj instanceof HeapDumpClassMirror) {
                // The MAT model doesn't expose member fields for classes.
                // All the fields on Class are caches though, so it's safe to start off null/0
                return null;
            } else {
                // Need to account for field shadowing manually
                IInstance heapDumpObject = ((HeapDumpInstanceMirror)obj).heapDumpObject;
                ClassMirror thisClass = obj.getClassMirror();
                for (Field field : heapDumpObject.getFields()) {
                    if (field.getName().equals(fieldDescriptor.getName())) {
                        // Move up the hierarchy until we find the next field of this name
                        for (;;) {
                            try {
                                thisClass.getDeclaredField(fieldDescriptor.getName());
                                break;
                            } catch (NoSuchFieldException e) {
                                thisClass = thisClass.getSuperClassMirror();
                            }
                        }
                        
                        if (thisClass.equals(declaringClass)) {
                            return field.getValue();
                        } else {
                            thisClass = thisClass.getSuperClassMirror();
                        }
                    }
                }
                
                throw new InternalError();
            }
        }
    }

    @Override
    public String getName() {
        return fieldDescriptor.getName();
    }
    
    public Class<?> getKlass() {
        switch (fieldDescriptor.getType()) {
        case IObject.Type.BOOLEAN: return Boolean.TYPE;
        case IObject.Type.BYTE: return Byte.TYPE;
        case IObject.Type.CHAR: return Character.TYPE;
        case IObject.Type.SHORT: return Short.TYPE;
        case IObject.Type.INT: return Integer.TYPE;
        case IObject.Type.LONG: return Long.TYPE;
        case IObject.Type.FLOAT: return Float.TYPE;
        case IObject.Type.DOUBLE: return Double.TYPE;
        default:
        case IObject.Type.OBJECT: return Object.class;
        }
    }
    
    @Override
    public ClassMirror getType() {
        return (ClassMirror)NativeInstanceMirror.makeMirror(getKlass());
    }
    
    @Override
    public int getModifiers() {
        throw new UnsupportedOperationException();
    }
    
    public ObjectMirror get(InstanceMirror obj) throws IllegalAccessException {
        Object value = getBoxedValue(obj);
        ObjectReference ref = (ObjectReference)value;
        if (ref == null) {
            return null;
        }
        return getObjectWithErrorHandling(vm, ref);
    }

    public static ObjectMirror getObjectWithErrorHandling(HeapDumpVirtualMachineMirror vm, ObjectReference ref) {
        try {
            return vm.makeMirror(ref.getObject());
        } catch (SnapshotException e) {
            // For now...
//            return new NativeInstanceMirror("(interned string @ " + Long.toHexString(ref.getObjectAddress()) + ")");
            throw new RuntimeException(e);
        }
    }
    
    private static String describePath(IObject owner, ObjectReference ref) throws SnapshotException, SecurityException, NoSuchFieldException {
        ISnapshot snapshot = owner.getSnapshot();
        IPathsFromGCRootsComputer computer = snapshot.getPathsFromGCRoots(owner.getObjectId(), Collections.<IClass, Set<String>>emptyMap());
        int[] shortestPath = computer.getNextShortestPath();
        StringBuilder builder = new StringBuilder();
        IObject root = snapshot.getObject(shortestPath[shortestPath.length - 1]);
        String rootLabel = (root instanceof IClass ? " == " + ((IClass)root).getName() : " : " + root.getClazz().getName());
        builder.append("(root" + rootLabel + ")");
        for (int from = shortestPath.length - 1; from > 0; from--) {
            builder.append(describeLink(snapshot, shortestPath[from], shortestPath[from - 1]));
        }
        if (ref instanceof NamedReference) {
            builder.append(((NamedReference)ref).getName());
        } else {
            builder.append(ref);
        }
        return builder.toString();
    }

    private static String describeLink(ISnapshot snapshot, int fromId, int toId) throws SnapshotException {
        IObject from = snapshot.getObject(fromId);
        IObject to = snapshot.getObject(toId);
        
        if (from instanceof IInstance) {
            IInstance instance = (IInstance)from;
            for (Field field : instance.getFields()) {
                Object value = field.getValue();
                if (value instanceof ObjectReference && ((ObjectReference)value).getObjectAddress() == to.getObjectAddress()) {
                    return "." + field.getName();
                }
            }
        }
        
        if (from instanceof IObjectArray) {
            IObjectArray array = (IObjectArray)from;
            long[] referenceArray = array.getReferenceArray();
            for (int i = 0; i < referenceArray.length; i++) {
                if (referenceArray[i] == to.getObjectAddress()) {
                    return "[" + i + "]";
                }
            }
        }
        
        if (from instanceof IClass) {
            IClass fromClass = (IClass)from;
            for (Field field : fromClass.getStaticFields()) {
                Object value = field.getValue();
                if (value instanceof ObjectReference && ((ObjectReference)value).getObjectAddress() == to.getObjectAddress()) {
                    return "." + field.getName();
                }
            }
        }
        
        IClass fromClass = from.getClazz();
        if (to.equals(fromClass)) {
            return ".getClass()";
        }
        
        for (NamedReference ref : from.getOutboundReferences()) {
            if (ref.getObjectAddress() == to.getObjectAddress()) {
                return "--" + ref.getName() + "-->";
            }
        }
        
        return "(?: from " + from.getClazz().getName() + " to " + to.getClazz().getName() + ")";
    }
    
    public void set(InstanceMirror obj, ObjectMirror o) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void setBoxedValue(InstanceMirror obj, Object o) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + fieldDescriptor.getVerboseSignature() + " " + fieldDescriptor.getName();
    }
}
