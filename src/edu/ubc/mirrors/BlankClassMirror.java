package edu.ubc.mirrors;

public abstract class BlankClassMirror extends BlankInstanceMirror implements ClassMirror {

    @Override
    public InstanceMirror getStaticFieldValues() {
        return new BlankInstanceMirror() {
            @Override
            public ClassMirror getClassMirror() {
                return getVM().findBootstrapClassMirror(Class.class.getName());
            }
        };
    }
}
