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
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.NativeObjectMirror;

public class HeapDumpFieldMirror extends BoxingFieldMirror {

    private final HeapDumpClassMirrorLoader loader;
    private final Field field;
    
    // For debugging only
    private final HeapDumpInstanceMirror ownerMirror;
    
    public HeapDumpFieldMirror(HeapDumpClassMirrorLoader loader, HeapDumpInstanceMirror ownerMirror, Field field) {
        this.loader = loader;
        this.ownerMirror = ownerMirror;
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
    
    public ObjectMirror get() throws IllegalAccessException {
        Object value = field.getValue();
        ObjectReference ref = (ObjectReference)value;
        if (ref == null) {
            return null;
        }
        NamedReference namedRef = new NamedReference(ownerMirror.getHeapDumpObject().getSnapshot(), ref.getObjectAddress(), "." + field.getName());
        return getObjectWithErrorHandling(ownerMirror, namedRef);
    }

    public static ObjectMirror getObjectWithErrorHandling(HeapDumpObjectMirror owner, ObjectReference ref) {
        try {
            HeapDumpClassMirrorLoader loader = owner.getClassMirror().getLoader();
            return HeapDumpInstanceMirror.makeMirror(loader, ref.getObject());
        } catch (SnapshotException e) {
//            try {
//                System.out.println("Bad address (" + ref.toString() + ") at: " + describePath(owner.getHeapDumpObject(), ref));
//            } catch (SnapshotException e1) {
//                // TODO Auto-generated catch block
//                e1.printStackTrace();
//            } catch (SecurityException e2) {
//                // TODO Auto-generated catch block
//                e2.printStackTrace();
//            } catch (NoSuchFieldException e3) {
//                // TODO Auto-generated catch block
//                e3.printStackTrace();
//            }
            
            // For now...
            return new NativeObjectMirror("(bad string literal?)");
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
