package edu.ubc.mirrors.sunhat;

import java.util.HashMap;
import java.util.Map;

import com.sun.tools.hat.internal.model.HackJavaValue;
import com.sun.tools.hat.internal.model.JavaClass;
import com.sun.tools.hat.internal.model.JavaObject;
import com.sun.tools.hat.internal.model.JavaObjectArray;
import com.sun.tools.hat.internal.model.JavaThing;
import com.sun.tools.hat.internal.model.JavaValueArray;
import com.sun.tools.hat.internal.model.Snapshot;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.sunhat.JHatClassMirror;
import edu.ubc.mirrors.sunhat.JHatInstanceMirror;
import edu.ubc.mirrors.sunhat.JHatObjectArrayMirror;
import edu.ubc.mirrors.raw.NativeBooleanArrayMirror;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.NativeCharArrayMirror;
import edu.ubc.mirrors.raw.NativeDoubleArrayMirror;
import edu.ubc.mirrors.raw.NativeFloatArrayMirror;
import edu.ubc.mirrors.raw.NativeIntArrayMirror;
import edu.ubc.mirrors.raw.NativeLongArrayMirror;
import edu.ubc.mirrors.raw.NativeShortArrayMirror;

public class JHatClassMirrorLoader extends ClassMirrorLoader {

    private final Snapshot snapshot;
    private final ClassLoader classLoader;
    
    private final Map<String, JHatClassMirror> classMirrors = new HashMap<String, JHatClassMirror>();
    
    public JHatClassMirrorLoader(Snapshot snapshot, ClassLoader loader) {
        this.snapshot = snapshot;
        this.classLoader = loader;
    }
    
    public ClassLoader getClassLoader() {
        return classLoader;
    }
    
    @Override
    public ClassMirror loadClassMirror(String name) throws ClassNotFoundException {
        JHatClassMirror mirror = classMirrors.get(name);
        if (mirror != null) {
            return mirror;
        }
        
        JavaClass javaClass = snapshot.findClass(name);
        mirror = new JHatClassMirror(this, javaClass);
        classMirrors.put(name, mirror);
        return mirror;
        
    }
    
    private final Map<JavaThing, ObjectMirror> mirrors = new HashMap<JavaThing, ObjectMirror>();
    
    public ObjectMirror getMirror(JavaThing object) {
        if (object == null) {
            return null;
        }
        
        ObjectMirror result = mirrors.get(object);
        if (result != null) {
            return result;
        }
        
        if (object instanceof HackJavaValue) {
            result = null;
        } else if (object instanceof JavaObject) {
            result = new JHatInstanceMirror(this, (JavaObject)object);
        } else if (object instanceof JavaObjectArray) {
            result = new JHatObjectArrayMirror(this, (JavaObjectArray)object);
        } else if (object instanceof JavaValueArray) {
            Object elements = ((JavaValueArray)object).getElements();
            if (elements instanceof boolean[]) {
                result = new NativeBooleanArrayMirror((boolean[])elements);
            } else if (elements instanceof byte[]) {
                result = new NativeByteArrayMirror((byte[])elements);
            } else if (elements instanceof char[]) {
                result = new NativeCharArrayMirror((char[])elements);
            } else if (elements instanceof short[]) {
                result = new NativeShortArrayMirror((short[])elements);
            } else if (elements instanceof int[]) {
                result = new NativeIntArrayMirror((int[])elements);
            } else if (elements instanceof long[]) {
                result = new NativeLongArrayMirror((long[])elements);
            } else if (elements instanceof float[]) {
                result = new NativeFloatArrayMirror((float[])elements);
            } else if (elements instanceof double[]) {
                result = new NativeDoubleArrayMirror((double[])elements);
            } else {
                throw new IllegalArgumentException("Unsupported array: " + elements.getClass());
            }
        } else {
            throw new IllegalArgumentException("Unsupported object: " + object.getClass());
        }
        
        mirrors.put(object, result);
        return result;
    }
    
}
