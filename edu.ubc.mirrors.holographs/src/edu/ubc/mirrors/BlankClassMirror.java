package edu.ubc.mirrors;

public abstract class BlankClassMirror extends BlankInstanceMirror implements ClassMirror {

    @Override
    public InstanceMirror getStaticFieldValues() {
        return new BlankStaticFieldValuesMirror();
    }
    
    private class BlankStaticFieldValuesMirror extends BlankInstanceMirror implements StaticFieldValuesMirror {
        @Override
        public ClassMirror getClassMirror() {
            return getVM().findBootstrapClassMirror(Object.class.getName());
        }
        
        @Override
        public ClassMirror forClassMirror() {
            return BlankClassMirror.this;
        }
    }
}
