package edu.ubc.mirrors.eclipse.mat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IClassLoader;
import org.eclipse.mat.snapshot.model.IInstance;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class HeapDumpClassMirror implements ClassMirror {

    private final HeapDumpVirtualMachineMirror vm;
    private final String className;
    // Will be null if this class was never actually loaded in the snapshot
    private final IClass klass;
    private final IClassLoader loader;
    
    
    public HeapDumpClassMirror(HeapDumpVirtualMachineMirror vm, IClass klass) {
        this.className = getClassName(klass);
        this.vm = vm;
        this.klass = klass;
        this.loader = getClassLoader(klass);
    }
    
    public HeapDumpClassMirror(HeapDumpVirtualMachineMirror vm, String className) {
        this.className = className;
        this.vm = vm;
        this.klass = null;
        this.loader = null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        
        HeapDumpClassMirror other = (HeapDumpClassMirror)obj;
        if (klass == null) {
            return className.equals(other.className) &&
                   (loader == null ? other.loader == null : loader.equals(other.loader));
        } else {
            return klass.equals(other.klass);
        }
    }
    
    @Override
    public int hashCode() {
        if (klass == null) {
            return 11 + className.hashCode();
        } else {
            return 11 + klass.hashCode();
        }
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
        return vm.getBytecodeClassMirror(this).getBytecode();
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
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        // TODO-RS: The MAT model doesn't expose member fields for classes.
        // Can we delegate to the bytecode implementation somehow?
        throw new UnsupportedOperationException();
    }
    
    @Override
    public HeapDumpClassMirrorLoader getLoader() {
        return (HeapDumpClassMirrorLoader)vm.makeMirror(loader);
    }

    public List<InstanceMirror> getInstances() {
        if (klass == null) {
            return Collections.emptyList();
        }
        
        List<InstanceMirror> result = new ArrayList<InstanceMirror>();
        try {
            for (int id : klass.getObjectIds()) {
                IInstance object = (IInstance)klass.getSnapshot().getObject(id);
                result.add((InstanceMirror)vm.makeMirror(object));
            }
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public List<FieldMirror> getMemberFields() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassMirror getClassMirror() {
        return vm.findBootstrapClassMirror(Class.class.getName());
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public boolean isPrimitive() {
        return vm.getBytecodeClassMirror(this).isPrimitive();
    }

    @Override
    public boolean isArray() {
        // This class is never instantiated for array types
        return false;
    }

    @Override
    public ClassMirror getComponentClassMirror() {
        return null;
    }

    @Override
    public ClassMirror getSuperClassMirror() {
        if (klass == null) {
            ClassMirror bytecodeClass = vm.getBytecodeClassMirror(this).getSuperClassMirror();
            return bytecodeClass == null ? null : vm.getClassMirrorForBytecodeClassMirror(bytecodeClass);
        } else {
            return klass.getSuperClass() == null ? null : new HeapDumpClassMirror(vm, klass.getSuperClass());
        }
    }

    @Override
    public boolean isInterface() {
        return vm.getBytecodeClassMirror(this).isInterface();
    }

    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        // Unfortunately the IClass interface doesn't expose the list of
        // implemented/extended interfaces. This makes it much more
        // difficult to figure out which exact classes they are in the presence
        // of multiple class loaders.
        List<ClassMirror> bytecodeClasses = vm.getBytecodeClassMirror(this).getInterfaceMirrors();
        List<ClassMirror> result = new ArrayList<ClassMirror>(bytecodeClasses.size());
        for (ClassMirror bytecodeClass : bytecodeClasses) {
            result.add(vm.getClassMirrorForBytecodeClassMirror(bytecodeClass));
        }
        return result;
    }

    @Override
    public List<String> getDeclaredFieldNames() {
        return vm.getBytecodeClassMirror(this).getDeclaredFieldNames();
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
    public InstanceMirror newRawInstance() {
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
    public String toString() {
        return getClass().getSimpleName() + ": " + klass;
    }
}
