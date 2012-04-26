package edu.ubc.mirrors.wrapping;

import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class WrappingClassMirror extends WrappingInstanceMirror implements ClassMirror {

    protected final ClassMirror wrapped;
    
    protected WrappingClassMirror(WrappingVirtualMachine vm, ClassMirror wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }
    
    @Override
    public VirtualMachineMirror getVM() {
        return vm;
    }
    
    @Override
    public String getClassName() {
        return wrapped.getClassName();
    }

    @Override
    public ClassMirrorLoader getLoader() {
        return (ClassMirrorLoader)vm.getWrappedMirror(wrapped.getLoader());
    }

    @Override
    public byte[] getBytecode() {
        return wrapped.getBytecode();
    }

    @Override
    public boolean isArray() {
        return wrapped.isArray();
    }

    @Override
    public ClassMirror getComponentClassMirror() {
        return vm.getWrappedClassMirror(wrapped.getComponentClassMirror());
    }

    @Override
    public ClassMirror getSuperClassMirror() {
        return vm.getWrappedClassMirror(wrapped.getSuperClassMirror());
    }

    @Override
    public boolean isInterface() {
        return wrapped.isInterface();
    }

    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        return vm.getWrappedClassMirrorList(wrapped.getInterfaceMirrors());
    }

    @Override
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        return vm.getFieldMirror(wrapped.getStaticField(name));
    }

    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        return vm.getFieldMirror(wrapped.getMemberField(name));
    }
    
    @Override
    public List<FieldMirror> getMemberFields() {
        return vm.getWrappedFieldList(wrapped.getMemberFields());
    }
    
    @Override
    public boolean isPrimitive() {
        return wrapped.isPrimitive();
    }
    
    @Override
    public List<String> getDeclaredFieldNames() {
        return wrapped.getDeclaredFieldNames();
    }
    
    @Override
    public List<InstanceMirror> getInstances() {
        List<InstanceMirror> instances = wrapped.getInstances();
        List<InstanceMirror> result = new ArrayList<InstanceMirror>(instances.size());
        for (InstanceMirror instance : instances) {
            result.add((InstanceMirror)vm.getWrappedMirror(instance));
        }
        return result;
    }

    @Override
    public MethodMirror getMethod(String name, ClassMirror... paramTypes)
            throws SecurityException, NoSuchMethodException {
        
        ClassMirror[] unwrappedParamTypes = new ClassMirror[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            unwrappedParamTypes[i] = (ClassMirror)vm.unwrapMirror(paramTypes[i]);
        }
        return new WrappingMethodMirror(vm, wrapped.getMethod(name, unwrappedParamTypes));
    }
    
    @Override
    public ConstructorMirror getConstructor(ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException {
        
        ClassMirror[] unwrappedParamTypes = new ClassMirror[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            unwrappedParamTypes[i] = (ClassMirror)vm.unwrapMirror(paramTypes[i]);
        }
        return new WrappingConstructorMirror(vm, wrapped.getConstructor(unwrappedParamTypes));
    }
    
}
