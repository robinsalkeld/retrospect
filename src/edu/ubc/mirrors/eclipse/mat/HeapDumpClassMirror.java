package edu.ubc.mirrors.eclipse.mat;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IClassLoader;
import org.eclipse.mat.snapshot.model.IInstance;
import org.eclipse.mat.snapshot.model.IObject;
import org.objectweb.asm.Type;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.fieldmap.ClassFieldMirror;
import edu.ubc.mirrors.mirages.MirageClassGenerator;

public class HeapDumpClassMirror implements ClassMirror {

    private final HeapDumpVirtualMachineMirror vm;
    private final IClass klass;
    private final IClassLoader loader;
    
    public HeapDumpClassMirror(HeapDumpVirtualMachineMirror vm, IClass klass) {
        if (klass == null) {
            throw new NullPointerException();
        }
        this.vm = vm;
        this.klass = klass;
        this.loader = getClassLoader(klass);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        
        HeapDumpClassMirror other = (HeapDumpClassMirror)obj;
        return klass.equals(other.klass);
    }
    
    @Override
    public int hashCode() {
        return 11 + klass.hashCode();
    }
    
    public static IClassLoader getClassLoader(IClass klass) {
        int loaderID = klass.getClassLoaderId();
        if (loaderID == 0) {
            return null;
        } else {
            try {
                return (IClassLoader)klass.getSnapshot().getObject(loaderID);
            } catch (SnapshotException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    @Override
    public VirtualMachineMirror getVM() {
        return vm;
    }
    
    @Override
    public byte[] getBytecode() {
        throw new UnsupportedOperationException();
    }
    
    public static String arrayElementDescriptor(String name) {
        if (name.equals("boolean")) {
            return "Z";
        } else if (name.equals("byte")) {
            return "B";
        } else if (name.equals("char")) {
            return "C";
        } else if (name.equals("short")) {
            return "S";
        } else if (name.equals("int")) {
            return "I";
        } else if (name.equals("long")) {
            return "J";
        } else if (name.equals("float")) {
            return "F";
        } else if (name.equals("double")) {
            return "D";
        } else {
            return "L" + name + ";";
        }
    }
    
    public static String getClassName(IClass klass) {
        String name = klass.getName();
        if (name.endsWith("[]")) {
            String elementName = name;
            String dimsString = "";
            while (elementName.endsWith("[]")) {
                elementName = elementName.substring(0, elementName.length() - 2);
                dimsString += "[";
            }
            name = dimsString + arrayElementDescriptor(elementName);
        }
        return name;
    }
    
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        List<Field> fields = klass.getStaticFields();
        for (Field field : fields) {
            if (field.getName().equals(name)) {
                return new HeapDumpFieldMirror(vm, field);
            }
        }
        throw new NoSuchFieldException(name);
    }

    @Override
    public FieldMirror getMemberField(final String name) throws NoSuchFieldException {
        // The MAT model doesn't expose member fields for classes.
        // All the fields on Class are caches though, so it's safe to start off null/0
        return new ClassFieldMirror(this, name);
    }
    
    @Override
    public HeapDumpClassMirrorLoader getLoader() {
        return (HeapDumpClassMirrorLoader)vm.makeMirror(loader);
    }

    public List<InstanceMirror> getInstances() {
        List<InstanceMirror> result = new ArrayList<InstanceMirror>();
        try {
            // TODO-RS: Handle interfaces as well
            addDirectInstances(result, klass);
            for (IClass subclass : klass.getAllSubclasses()) {
                addDirectInstances(result, subclass);
            }
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private void addDirectInstances(List<InstanceMirror> list, IClass klass) throws SnapshotException {
        for (int id : klass.getObjectIds()) {
            IObject object = (IObject)klass.getSnapshot().getObject(id);
            InstanceMirror mirror = (InstanceMirror)vm.makeMirror(object);
            if (mirror != null) {
                list.add(mirror);
            }
        }
    }
    
    @Override
    public List<FieldMirror> getMemberFields() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassMirror getClassMirror() {
        return vm.findBootstrapClassMirror(Class.class.getName());
    }

    private String className;
    
    @Override
    public String getClassName() {
        if (className == null) {
            className = getClassName(klass);
        }
        return className;
    }

    @Override
    public boolean isPrimitive() {
        // Primitive classes don't show up in the dump
        return false;
    }

    @Override
    public boolean isArray() {
        return klass.isArrayType();
    }

    @Override
    public ClassMirror getComponentClassMirror() {
        String name = getClassName();
        if (name.startsWith("[")) {
            Type componentType = Type.getType(name.substring(1).replace('.', '/'));
            if (MirageClassGenerator.isRefType(componentType)) {
                String componentClassName = componentType.getInternalName().replace('/', '.');
                if (loader == null) {
                    return vm.findBootstrapClassMirror(componentClassName);
                } else {
                    return getLoader().findLoadedClassMirror(componentClassName);    
                }
            } else {
                return vm.getPrimitiveClass(componentType.getClassName());
            }
        } else {
            return null;
        }
    }

    @Override
    public ClassMirror getSuperClassMirror() {
        return klass.getSuperClass() == null ? null : new HeapDumpClassMirror(vm, klass.getSuperClass());
    }

    @Override
    public boolean isInterface() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, ClassMirror> getDeclaredFields() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodMirror getMethod(String name, ClassMirror... paramTypes)
            throws SecurityException, NoSuchMethodException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ConstructorMirror getConstructor(ClassMirror... paramTypes)
            throws SecurityException, NoSuchMethodException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int getModifiers() {
        // TODO-RS: Let's pretend for now
        return Modifier.PUBLIC;
    }
    
    @Override
    public InstanceMirror newRawInstance() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ClassMirrorLoader newRawClassLoaderInstance() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ArrayMirror newArray(int size) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ArrayMirror newArray(int... dims) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean initialized() {
        // Unfortunately the model doesn't track this
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + klass;
    }
}
