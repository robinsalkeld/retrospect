package edu.ubc.mirrors.mirages;

import java.util.ArrayList;
import java.util.List;

import org.jruby.org.objectweb.asm.Type;

// TODO-RS: This almost exactly mirrors (ha) the ClassMirrorLoader/ClassMirror types. Use them instead?
public interface ClassHierarchy {

    public Node getNode(String internalClassName) throws ClassNotFoundException;
    
    public static abstract class Node {
        
        public abstract ClassHierarchy getHierarchy();
        
        protected Node getNodeInternal(String internalClassName) {
            try {
                return getHierarchy().getNode(internalClassName);
            } catch (ClassNotFoundException e) {
                NoClassDefFoundError error = new NoClassDefFoundError(e.getMessage());
                error.initCause(e);
                throw error;
            }
        }
        
        public abstract String getInternalClassName();
        
        public abstract Node getSuperclass();
        
        public abstract List<Node> getInterfaces();
        
        public abstract boolean isInterface();
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Node)) {
                return false;
            }
            
            return getInternalClassName().equals(((Node)obj).getInternalClassName());
        }
        
        @Override
        public int hashCode() {
            return 7 + getInternalClassName().hashCode();
        }
        
        public boolean isAssignableFrom(Node other) {
            if (equals(other)) {
                return true;
            }
            
            Node otherSuperclass = other.getSuperclass();
            if (otherSuperclass != null && isAssignableFrom(otherSuperclass)) {
                return true;
            }

            for (Node interfaceNode : other.getInterfaces()) {
                if (isAssignableFrom(interfaceNode)) {
                    return true;
                }
            }
            
            return false;
        }
        
    }
    
    public abstract static class ClassNode extends Node {

        private final Class<?> klass;
        private Node superClass;
        private List<Node> interfaces;
        
        public ClassNode(Class<?> klass) {
            this.klass = klass;
            Class<?> realSuperClass = klass.getSuperclass();
            this.superClass = realSuperClass == null ? null : getNodeInternal(realSuperClass.getName());
        }
        
        @Override
        public String getInternalClassName() {
            return Type.getInternalName(klass);
        }

        @Override
        public Node getSuperclass() {
            return superClass;
        }

        @Override
        public List<Node> getInterfaces() {
            if (interfaces == null) {
                Class<?>[] classInterfaces = klass.getInterfaces();
                interfaces = new ArrayList<Node>(classInterfaces.length);
                for (Class<?> i : klass.getInterfaces()) {
                    interfaces.add(getNodeInternal(Type.getInternalName(i)));
                }
            }
            return interfaces;
        }

        @Override
        public boolean isInterface() {
            return klass.isInterface();
        }
        
    }
    
}
