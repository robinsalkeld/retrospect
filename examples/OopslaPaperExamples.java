package examples;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IClassLoader;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;
import org.eclipse.mat.util.ConsoleProgressListener;
import org.eclipse.osgi.framework.internal.core.BundleRepository;
import org.osgi.framework.Bundle;

import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ChainedClassMirrorLoader;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpClassMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpClassMirrorLoader;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.mutable.MutableClassMirrorLoader;
import edu.ubc.mirrors.raw.BytecodeClassMirrorLoader;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;
import edu.ubc.mirrors.test.EclipseHeapDumpTest;
import edu.ubc.mirrors.test.PrintOSGiBundles;

public class OopslaPaperExamples {

public List<ServiceProperty> getProperties(IObject propertiesObject) {
    String[] keys = null;
    String[] values = null;
    
    IObjectArray keysArray = 
            (IObjectArray) propertiesObject.resolveValue("headers");
    if (keysArray != null) {
        long[] keyAddresses = keysArray.getReferenceArray();
        if (keyAddresses != null) {
            keys = getServiceProperties(keyAddresses);
        }
    }
    
    IObjectArray valuesArray = /* similar */;
    
    if (keys == null || values == null)
        return null;
    List<ServiceProperty> properties = 
        new ArrayList<ServiceProperty>(keys.length);
    for (int i = 0; i < keys.length; i++) {
        properties.add(new ServiceProperty(keys[i], values[i]));
    }
    return properties;
}


public static void arraycopy(Mirror src, int srcPos, 
                             Mirror dest, int destPos, int length) {
    for (int off = 0; off < length; off++) {
        setArrayElement(dest, destPos + off, getArrayElement(src, srcPos + off));
    }
}
    
private static Object getArrayElement(ObjectMirror am, int index) {
    if (am instanceof ObjectArrayMirror) {
        return ((ObjectArrayMirror)am).get(index);
    } else if (am instanceof ByteArrayMirror) {
        return ((ByteArrayMirror)am).getByte(index);
    } else if (am instanceof CharArrayMirror) {
        return ((CharArrayMirror)am).getChar(index);
    ...
}
    
private static void setArrayElement(Mirror am, int index, Object o) {
    if (am instanceof ObjectArrayMirror) {
        ((ObjectArrayMirror)am).set(index, (Mirror)o);
    } else if (am instanceof ByteArrayMirror) {
        ((ByteArrayMirror)am).setByte(index, (Byte)o);
    } else if (am instanceof BooleanArrayMirror) {
        ((CharArrayMirror)am).setChar(index, (Character)o);
    ...
}

    
public static String toString(IInstance heapDumpObject) {
    // Create a holograph from the Eclipse MAT IInstance object
    InstanceMirror mirror = new HeapDumpHolograph(heapDumpObject);
    
    // Invoke the toString() method on the holograph
    ClassMirror objectClass = mirror.getClassMirror()
            .getLoader().loadClassMirror("java.lang.Object");
    MethodMirror method = objectClass.getMethod("toString");
    Mirror stringMirror = toStringMethod.invoke(mirror);
    
    // Create a new String with the same contents as the result.
    return Reflection.stringFromStringMirror(stringMirror);
}
