package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageBinaryClassName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageInterfaces;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageInternalClassName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageSuperclassName;
import static edu.ubc.mirrors.mirages.MirageClassGenerator.getOriginalBinaryClassName;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.FieldMirror;

public class MirageClassMirrorLoader extends ClassMirrorLoader {

    ClassMirrorLoader originalLoader;
    
    ClassLoaderLiteralMirror classLoaderLiteralMirror = new ClassLoaderLiteralMirror(this);
    
    
    public MirageClassMirrorLoader(ClassMirrorLoader parent, ClassMirrorLoader originalLoader) {
        super(parent);
        this.originalLoader = originalLoader;
    }
    
    @Override
    public ClassMirror loadClassMirror(String name) throws ClassNotFoundException {
        if (name.startsWith("mirage") && !name.startsWith("miragearray")) {
            String originalClassName = getOriginalBinaryClassName(name);
            ClassMirror original = originalLoader.loadClassMirror(originalClassName);
            return new MirageClassMirror(name, original);
        } else if (name.equals(ClassLoaderLiteralMirror.CLASS_LOADER_LITERAL_NAME)) {
            return classLoaderLiteralMirror;
        }
        
        return super.loadClassMirror(name);
    }
    
    private class MirageClassMirror extends ClassMirror {

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
                superclassNode = superName == null ? null : loadClassMirrorInternal(getMirageSuperclassName(isInterface, mirageName, mirageSuperName).replace('/', '.'));
                
                for (int i = 0; i < interfaces.length; i++) {
                    interfaces[i] = getMirageInternalClassName(interfaces[i], false);
                }
                interfaceNodes = new ArrayList<ClassMirror>(interfaces.length);
                for (String i : getMirageInterfaces(isInterface, interfaces)) {
                    interfaceNodes.add(loadClassMirrorInternal(i.replace('/', '.')));
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
            // TODO-RS: Implement
            return null;
        }
        
        @Override
        public FieldMirror getMemberField(String name) throws NoSuchFieldException {
            // TODO-RS: Implement
            return null;
        }

        @Override
        public List<FieldMirror> getMemberFields() {
            // TODO-RS: Implement
            return null;
        }
        
        @Override
        public ClassMirrorLoader getLoader() {
            return MirageClassMirrorLoader.this;
        }
        
        @Override
        public boolean isPrimitive() {
            return false;
        }
        
        @Override
        public Class<?> getNativeStubsClass() {
            return null;
        }
    }
}
