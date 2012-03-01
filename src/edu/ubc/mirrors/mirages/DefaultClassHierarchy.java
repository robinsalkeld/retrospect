package edu.ubc.mirrors.mirages;


public class DefaultClassHierarchy implements ClassHierarchy {

    private final ClassLoader loader;
    
    public DefaultClassHierarchy(ClassLoader loader) {
        this.loader = loader;
    }
    
    @Override
    public Node getNode(String internalClassName) throws ClassNotFoundException {
        return new ClassNode(loader.loadClass(internalClassName.replace('/', '.'))) {
            @Override
            public ClassHierarchy getHierarchy() {
                // TODO Auto-generated method stub
                return DefaultClassHierarchy.this;
            }
        };
    }

    
}
