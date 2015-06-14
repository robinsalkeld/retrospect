package edu.ubc.mirrors.tod;

import java.util.ArrayList;
import java.util.List;

import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.MirrorLocation;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;

public class TODMethodOrConstructorMirror implements ConstructorMirror, MethodMirror {

    private final TODVirtualMachineMirror vm;
    protected final IBehaviorInfo behaviourInfo;
    
    public TODMethodOrConstructorMirror(TODVirtualMachineMirror vm, IBehaviorInfo behaviourInfo) {
        this.vm = vm;
        this.behaviourInfo = behaviourInfo;
    }
    
    @Override
    public byte[] getBytecode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AnnotationMirror> getAnnotations(ThreadMirror thread) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<List<AnnotationMirror>> getParameterAnnotations(ThreadMirror thread) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getDefaultValue(ThreadMirror thread) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassMirror getDeclaringClass() {
        return vm.makeClassMirror(behaviourInfo.getDeclaringType());
    }

    @Override
    public int getModifiers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return behaviourInfo.getName();
    }

    @Override
    public List<String> getParameterTypeNames() {
        List<String> result = new ArrayList<String>();
        for (ITypeInfo type : behaviourInfo.getArgumentTypes()) {
            result.add(vm.makeClassMirror(type).getClassName());
        }
        return result;
    }

    @Override
    public List<ClassMirror> getParameterTypes() {
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        for (ITypeInfo type : behaviourInfo.getArgumentTypes()) {
            result.add(vm.makeClassMirror(type));
        }
        return result;
    }

    @Override
    public List<String> getExceptionTypeNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ClassMirror> getExceptionTypes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getReturnTypeName() {
        return getReturnType().getClassName();
    }

    @Override
    public ClassMirror getReturnType() {
        return vm.makeClassMirror(behaviourInfo.getReturnType());
    }

    @Override
    public String getSignature() {
        return behaviourInfo.getSignature();
    }

    @Override
    public Object invoke(ThreadMirror thread, ObjectMirror obj, Object... args)
            throws IllegalArgumentException, IllegalAccessException,
            MirrorInvocationTargetException {

        // TODO-RS: Technically TOD implements a minimal version of holographic execution,
        // but no need to actually hook it up here.
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAccessible(boolean flag) {
        // TODO-RS: Nothing to do - I still think this method probably shouldn't be in the API...
    }

    @Override
    public MirrorLocation locationForBytecodeOffset(int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InstanceMirror newInstance(ThreadMirror thread, Object... args)
            throws IllegalAccessException, IllegalArgumentException,
            MirrorInvocationTargetException {
        
        // TODO-RS: Technically TOD implements a minimal version of holographic execution,
        // but no need to actually hook it up here.
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + getName();
    }
}
