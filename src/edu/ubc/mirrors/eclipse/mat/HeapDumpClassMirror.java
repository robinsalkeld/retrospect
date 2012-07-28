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
import org.objectweb.asm.Type;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.BoxingFieldMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.raw.ArrayClassMirror;
import edu.ubc.mirrors.wrapping.WrapperAware;
import edu.ubc.mirrors.wrapping.WrappingMirror;

public class HeapDumpClassMirror implements ClassMirror, WrapperAware {

    private final HeapDumpVirtualMachineMirror vm;
    // Will be null if this class was never actually loaded in the snapshot
    private final IClass klass;
    private final IClassLoader loader;
    
    private ClassMirror wrapper;
    private ClassMirror bytecodeMirror;
    
    public HeapDumpClassMirror(HeapDumpVirtualMachineMirror vm, IClass klass) {
        if (klass == null) {
            throw new NullPointerException();
        }
        this.vm = vm;
        this.klass = klass;
        this.loader = getClassLoader(klass);
    }
    
    public HeapDumpClassMirror(HeapDumpVirtualMachineMirror vm, IClassLoader loader, ClassMirror bytecodeMirror) {
        this.bytecodeMirror = bytecodeMirror;
        this.vm = vm;
        this.klass = null;
        this.loader = loader;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        
        HeapDumpClassMirror other = (HeapDumpClassMirror)obj;
        if (klass == null) {
            return getBytecodeMirror().equals(other.getBytecodeMirror()) &&
                   (loader == null ? other.loader == null : loader.equals(other.loader));
        } else {
            return klass.equals(other.klass);
        }
    }
    
    @Override
    public int hashCode() {
        if (klass == null) {
            return 11 + getBytecodeMirror().hashCode();
        } else {
            return 11 + klass.hashCode();
        }
    }
    
    private ClassMirror getBytecodeMirror(ClassMirror klass) {
        if (klass.isPrimitive()) {
            return vm.getPrimitiveClass(klass.getClassName());
        } else if (klass instanceof HeapDumpClassMirror) {
            return ((HeapDumpClassMirror)klass).getBytecodeMirror();
        } else if (klass instanceof ArrayClassMirror) {
            ArrayClassMirror arrayClass = (ArrayClassMirror)klass;
            ClassMirror elementBytecodeMirror = getBytecodeMirror(arrayClass.getElementClassMirror());
            return new ArrayClassMirror(arrayClass.getDimensions(), elementBytecodeMirror);
        } else {
            throw new IllegalArgumentException(klass.toString());
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
        return getBytecodeMirror().getBytecode();
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
        if (klass == null) {
            return getBytecodeMirror().getStaticField(name);
        }
        
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
        return new ClassFieldMirror(this, name);
    }
    
    // The MAT model doesn't expose member fields for classes.
    // All the fields on Class are caches though, so it's safe to start off null/0
    private static class ClassFieldMirror extends BoxingFieldMirror {
        private final HeapDumpClassMirror klass;
        private final String name;
        
        public ClassFieldMirror(HeapDumpClassMirror klass, String name) {
            this.klass = klass;
            this.name = name;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ClassFieldMirror)) {
                return false;
            }
            
            ClassFieldMirror other = (ClassFieldMirror)obj;
            return klass.equals(other.klass) && name.equals(other.name);
        }
        
        @Override
        public int hashCode() {
            return klass.hashCode() * 32 + name.hashCode();
        }
        
        @Override
        public String getName() {
            return name;
        }
        @Override
        public ClassMirror getType() {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public ObjectMirror get() throws IllegalAccessException {
            return null;
        }
        @Override
        public Object getBoxedValue() throws IllegalAccessException {
            return null;
        }
        @Override
        public void set(ObjectMirror o) throws IllegalAccessException {
            throw new UnsupportedOperationException();
        }
        @Override
        public void setBoxedValue(Object o) throws IllegalAccessException {
            throw new UnsupportedOperationException();
        }
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
                InstanceMirror mirror = (InstanceMirror)vm.makeMirror(object);
                if (mirror != null) {
                    result.add(mirror);
                }
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

    private String className;
    
    @Override
    public String getClassName() {
        if (className == null) {
            if (klass == null) {
                className = getBytecodeMirror().getClassName();
            } else {
                className = getClassName(klass);
            }
        }
        return className;
    }

    @Override
    public boolean isPrimitive() {
        return getBytecodeMirror().isPrimitive();
    }

    @Override
    public boolean isArray() {
        if (klass == null) {
            return getBytecodeMirror().isArray();
        } else {
            return klass.isArrayType();
        }
    }

    @Override
    public ClassMirror getComponentClassMirror() {
        if (klass == null) {
            return getBytecodeMirror().getComponentClassMirror();
        } else {
            String name = getClassName();
            if (name.startsWith("[")) {
                String componentName = name.substring(2, name.length() - 1);
                if (loader == null) {
                    return vm.findBootstrapClassMirror(componentName);
                } else {
                    return getLoader().findLoadedClassMirror(componentName);    
                }
            } else {
                return null;
            }
        }
    }

    @Override
    public ClassMirror getSuperClassMirror() {
        if (klass == null) {
            return getBytecodeMirror().getSuperClassMirror();
        } else {
            return klass.getSuperClass() == null ? null : new HeapDumpClassMirror(vm, klass.getSuperClass());
        }
    }

    @Override
    public boolean isInterface() {
        return getBytecodeMirror().isInterface();
    }

    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        return getBytecodeMirror().getInterfaceMirrors();
    }

    @Override
    public Map<String, ClassMirror> getDeclaredFields() {
        return getBytecodeMirror().getDeclaredFields();
    }

    private ClassMirror getBytecodeMirror() {
        if (bytecodeMirror == null) {
            bytecodeMirror = vm.getBytecodeClassMirror(this);
        }
        return bytecodeMirror;
    }

    @Override
    public MethodMirror getMethod(String name, ClassMirror... paramTypes)
            throws SecurityException, NoSuchMethodException {
        
        ClassMirror[] bytecodeParamTypes = new ClassMirror[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            bytecodeParamTypes[i] = getBytecodeMirror(paramTypes[i]);
        }
        
        return getBytecodeMirror().getMethod(name, bytecodeParamTypes);
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
    public ArrayMirror newArray(int size) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ArrayMirror newArray(int... dims) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean initialized() {
        if (klass == null) {
            return getBytecodeMirror().initialized();
        } else {
            return true;
        }
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + (klass == null ? getBytecodeMirror() : klass);
    }

    @Override
    public void setWrapper(WrappingMirror mirror) {
        this.wrapper = (ClassMirror)mirror;
    }
    
    public ClassMirror getWrapper() {
        return wrapper;
    }
}
