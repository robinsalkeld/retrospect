package edu.ubc.mirrors.eclipse.mat;

import java.util.Collections;
import java.util.Set;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.IPathsFromGCRootsComputer;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;
import org.eclipse.mat.snapshot.model.NamedReference;
import org.eclipse.mat.snapshot.model.ObjectReference;

import edu.ubc.mirrors.BoxingFieldMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.raw.NativeObjectMirror;

public class HeapDumpFieldMirror extends BoxingFieldMirror {

    private final Field field;
    
    private final HeapDumpClassMirrorLoader loader;
    
    public HeapDumpFieldMirror(HeapDumpClassMirrorLoader loader, Field field) {
        this.loader = loader;
        this.field = field;
    }
    
    @Override
    public Object getBoxedValue() throws IllegalAccessException {
        return field.getValue();
    }
    
    @Override
    public String getName() {
        return field.getName();
    }
    
    @Override
    public Class<?> getType() {
        switch (field.getType()) {
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
    
    public ObjectMirror get() throws IllegalAccessException {
        Object value = field.getValue();
        ObjectReference ref = (ObjectReference)value;
        if (ref == null) {
            return null;
        }
        return getObjectWithErrorHandling(loader, ref);
    }

    public static ObjectMirror getObjectWithErrorHandling(HeapDumpClassMirrorLoader loader, ObjectReference ref) {
        try {
            return HeapDumpInstanceMirror.makeMirror(loader, ref.getObject());
        } catch (SnapshotException e) {
            // For now...
            return new NativeObjectMirror("(interned string @ " + Long.toHexString(ref.getObjectAddress()) + ")");
//            throw new RuntimeException(e);
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
    
    public void set(ObjectMirror o) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void setBoxedValue(Object o) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }
    
}
