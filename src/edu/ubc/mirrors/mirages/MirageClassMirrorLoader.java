package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageBinaryClassName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageInterfaces;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageInternalClassName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageSuperclassName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getOriginalBinaryClassName;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;
import edu.ubc.mirrors.raw.SandboxedClassLoader;

public class MirageClassMirrorLoader implements ClassMirrorLoader {

    VirtualMachineMirror vm;
    ClassMirrorLoader parent;
    
    ClassMirrorLoader originalLoader;
    
    ClassLoaderLiteralMirror classLoaderLiteralMirror = new ClassLoaderLiteralMirror(vm, this);
    
    final VirtualMachineMirror mirageVM = new VirtualMachineMirror() {
        @Override
        public ClassMirror findBootstrapClassMirror(String name) {
            ClassMirror result = parent.findLoadedClassMirror(name);
            if (result != null) {
                return result;
            }
                        
            if (name.startsWith("mirage") && !name.startsWith("miragearray")) {
                String originalClassName = getOriginalBinaryClassName(name);
                ClassMirror original = vm.findBootstrapClassMirror(originalClassName);
                return new MirageClassMirror(name, original);
            } else if (name.equals(ClassLoaderLiteralMirror.CLASS_LOADER_LITERAL_NAME)) {
                return classLoaderLiteralMirror;
            } else {
                return null;
            }
        }

        @Override
        public List<ClassMirror> findAllClasses(String name) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public ClassMirror getPrimitiveClass(String name) {
            return vm.getPrimitiveClass(name);
        }
        
        @Override
        public ClassMirror getArrayClass(int dimensions, ClassMirror elementClass) {
            return vm.getArrayClass(dimensions, elementClass);
        }
    };
    
    public VirtualMachineMirror getMirageVM() {
        return mirageVM;
    }
    
    // We need a more legitimate loader to expose as the actual object loading these classes,
    // even though the classes will never be executed. Something has to be available to run
    // the mirage-lifted findClass() method on.
    private final ClassMirrorLoader nativeMirror;
    
    public MirageClassMirrorLoader(VirtualMachineMirror vm, ClassMirrorLoader parent, ClassMirrorLoader originalLoader) {
        this.vm = vm;
        this.parent = parent;
        this.originalLoader = originalLoader;
        
        // Use a sandboxed class loader that can't load anything so that the mirage class loader
        // only loads what the findLoadedClassMirror (i.e. the native findLoadedClass() method) returns.
        this.nativeMirror = new NativeClassMirrorLoader(new SandboxedClassLoader(new URL[0])) {
            @Override
            public ClassMirror findLoadedClassMirror(String name) {
                return MirageClassMirrorLoader.this.findLoadedClassMirror(name);
            }
        };
    }
    
    public ClassMirror findLoadedClassMirror(String name) {
        ClassMirror result = parent.findLoadedClassMirror(name);
        if (result != null) {
            return result;
        }
                    
        if (name.startsWith("mirage") && !name.startsWith("miragearray")) {
            String originalClassName = getOriginalBinaryClassName(name);
            ClassMirror original;
            try {
                original = Reflection.loadClassMirror(vm, originalLoader, originalClassName);
            } catch (ClassNotFoundException e) {
                throw new NoClassDefFoundError(e.getMessage());
            }
            return new MirageClassMirror(name, original);
        } else if (name.equals(ClassLoaderLiteralMirror.CLASS_LOADER_LITERAL_NAME)) {
            return classLoaderLiteralMirror;
        } else {
            return null;
        }
    }
    
    private class MirageClassMirror implements ClassMirror {

        private final String className;
        private final ClassMirror originalMirror;
        
        private boolean resolved = false;
        
        private ClassMirror superclassNode;
        private List<ClassMirror> interfaceNodes;
        private boolean isInterface;
        
        public MirageClassMirror(String name, ClassMirror original) {
            this.className = getMirageBinaryClassName(name, false);
            this.originalMirror = original;
        }

        private class Stop extends RuntimeException {}
        
        private class Visitor extends ClassVisitor {

            public Visitor() {
                super(Opcodes.ASM4);
            }
            
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                isInterface = (Opcodes.ACC_INTERFACE & access) != 0;
                
                String mirageName = getMirageInternalClassName(name, true);
                String mirageSuperName = getMirageInternalClassName(superName, false);
                superclassNode = superName == null ? null : Reflection.loadClassMirrorInternal(MirageClassMirror.this, getMirageSuperclassName(isInterface, mirageName, mirageSuperName).replace('/', '.'));
                
                for (int i = 0; i < interfaces.length; i++) {
                    interfaces[i] = getMirageInternalClassName(interfaces[i], false);
                }
                interfaceNodes = new ArrayList<ClassMirror>(interfaces.length);
                for (String i : getMirageInterfaces(isInterface, interfaces)) {
                    interfaceNodes.add(Reflection.loadClassMirrorInternal(MirageClassMirror.this, i.replace('/', '.')));
                }
                
                throw new Stop();
            }
            
        }
        
        // TODO-RS: 
        private void resolve() {
            if (resolved) {
                return;
            }
            
            try {
                new ClassReader(originalMirror.getBytecode()).accept(new Visitor(), 0);
            } catch (Stop e) {
                // expected
            }
            
            resolved = true;
        }
        
        @Override
        public String getClassName() {
            return className;
        }

        @Override
        public byte[] getBytecode() {
            // TODO-RS: Implement
            return null;
        }

        @Override
        public boolean isArray() {
            return false;
        }

        @Override
        public ClassMirror getComponentClassMirror() {
            return null;
        }

        @Override
        public ClassMirror getSuperClassMirror() {
            resolve();
            return superclassNode;
        }

        @Override
        public boolean isInterface() {
            resolve();
            return isInterface;
        }

        @Override
        public List<ClassMirror> getInterfaceMirrors() {
            resolve();
            return interfaceNodes;
        }

        @Override
        public FieldMirror getStaticField(String name) throws NoSuchFieldException {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public FieldMirror getMemberField(String name) throws NoSuchFieldException {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<FieldMirror> getMemberFields() {
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
        public List<String> getDeclaredFieldNames() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public ClassMirrorLoader getLoader() {
            return MirageClassMirrorLoader.this;
        }
        
        @Override
        public VirtualMachineMirror getVM() {
            return vm;
        }
        
        @Override
        public boolean isPrimitive() {
            return false;
        }
        
        @Override
        public List<InstanceMirror> getInstances() {
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
        public ClassMirror getClassMirror() {
            return getVM().findBootstrapClassMirror(Class.class.getName());
        }
    }
    
    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        return nativeMirror.getMemberField(name);
    }

    @Override
    public List<FieldMirror> getMemberFields() {
        return nativeMirror.getMemberFields();
    }

    @Override
    public ClassMirror getClassMirror() {
        return nativeMirror.getClassMirror();
    }
    
    @Override
    public ClassMirror defineClass1(String name, ByteArrayMirror b, int off,
            int len, InstanceMirror pd, InstanceMirror source, boolean verify) {
        throw new UnsupportedOperationException();
    }
}
