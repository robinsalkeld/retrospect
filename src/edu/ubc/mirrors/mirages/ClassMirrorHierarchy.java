package edu.ubc.mirrors.mirages;

import static edu.ubc.mirrors.mirages.MirageClassGenerator.getMirageInterfaces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jruby.org.objectweb.asm.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.mirages.ClassHierarchy.Node;

public class ClassMirrorHierarchy implements ClassHierarchy {

    private final ClassMirrorLoader loader;
    
    private final Map<String, Node> nodes = new HashMap<String, Node>();
    
    public ClassMirrorHierarchy(ClassMirrorLoader loader) {
        this.loader = loader;
    }
    
    @Override
    public Node getNode(String internalClassName) throws ClassNotFoundException {
        Node result = nodes.get(internalClassName);
        if (result == null) {
            result = new MirrorNode(loader.loadClassMirror(internalClassName.replace('/', '.')));
            nodes.put(internalClassName, result);
        }
        return result;
    }
    
    private class MirrorNode extends Node {
        
        private final ClassMirror mirror;
        private List<Node> interfaces;
        
        public MirrorNode(ClassMirror mirror) {
            this.mirror = mirror;
        }

        @Override
        public ClassHierarchy getHierarchy() {
            return ClassMirrorHierarchy.this;
        }

        @Override
        public String getInternalClassName() {
            return mirror.getClassName().replace('.', '/');
        }

        @Override
        public Node getSuperclass() {
            ClassMirror superClassMirror = mirror.getSuperClassMirror();
            return superClassMirror == null ? null : getNodeInternal(superClassMirror.getClassName().replace('.', '/'));
        }

        @Override
        public List<Node> getInterfaces() {
            if (interfaces == null) {
                List<ClassMirror> classInterfaces = mirror.getInterfaceMirrors();
                interfaces = new ArrayList<Node>(classInterfaces.size());
                for (ClassMirror i : classInterfaces) {
                    interfaces.add(getNodeInternal(i.getClassName().replace('.', '/')));
                }
            }
            return interfaces;
        }

        @Override
        public boolean isInterface() {
            return mirror.isInterface();
        }
        
    }
}
