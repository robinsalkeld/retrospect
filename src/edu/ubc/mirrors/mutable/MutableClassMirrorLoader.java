package edu.ubc.mirrors.mutable;

import java.util.HashMap;
import java.util.Map;

import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

public class MutableClassMirrorLoader extends ClassMirrorLoader {

    private final Map<ObjectMirror, ObjectMirror> mirrors = new HashMap<ObjectMirror, ObjectMirror>();
    private final ClassMirrorLoader immutableLoader;
    
    public MutableClassMirrorLoader(ClassMirrorLoader immutableLoader) {
        this.immutableLoader = immutableLoader;
    }
    
    @Override
    public ClassMirror loadClassMirror(String name) throws ClassNotFoundException {
        return (ClassMirror)makeMirror(immutableLoader.loadClassMirror(name));
    }
    
    public ObjectMirror makeMirror(ObjectMirror immutableMirror) {
        if (immutableMirror == null) {
            return null;
        }
        
        ObjectMirror result = mirrors.get(immutableMirror);
        if (result != null) {
            return result;
        }
        
        if (immutableMirror instanceof ClassMirror) {
            result = new MutableClassMirror(this, (ClassMirror)immutableMirror);
        } else if (immutableMirror instanceof InstanceMirror) {
            result = new MutableInstanceMirror(this, (InstanceMirror)immutableMirror);
        } else if (immutableMirror instanceof ObjectArrayMirror) {
            result = new MutableObjectArrayMirror(this, (ObjectArrayMirror)immutableMirror);
        // TODO: fix - should check immutableMirror.getClassMirror().getClassName() instead!
        } else if (immutableMirror instanceof CharArrayMirror){
            result = new MutableCharArrayMirror((CharArrayMirror)immutableMirror);
        } else {
            throw new IllegalArgumentException("Unsupported subclass: " + immutableMirror.getClass());
        }
        
        mirrors.put(immutableMirror, result);
        return result;
    }
    
    private static Map<ClassMirrorLoader, MutableClassMirrorLoader> loaders = new
               HashMap<ClassMirrorLoader, MutableClassMirrorLoader>();
    
    public static ObjectMirror makeMirrorStatic(ObjectMirror mirror) {
        ClassMirrorLoader loader = mirror.getClassMirror().getLoader();
        MutableClassMirrorLoader mutableLoader = loaders.get(loader);
        if (mutableLoader == null) {
            mutableLoader = new MutableClassMirrorLoader(loader);
            loaders.put(loader, mutableLoader);
        }
        return mutableLoader.makeMirror(mirror);
    }
}
