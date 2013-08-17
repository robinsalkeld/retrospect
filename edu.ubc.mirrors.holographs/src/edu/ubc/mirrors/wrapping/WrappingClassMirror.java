package edu.ubc.mirrors.wrapping;

import java.io.File;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class WrappingClassMirror extends WrappingInstanceMirror implements ClassMirror {

    // Not final for the benefit of ClassHolograph, which changes targets dynamically.
    // TODO-RS: Could that be implemented more cleanly (but less efficiently)
    // by replacing references to the ClassHolograph everywhere else instead?
    protected ClassMirror wrapped;
    
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
    public String getSignature() {
        return wrapped.getSignature();
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
    public byte[] getRawAnnotations() {
        return wrapped.getRawAnnotations();
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
    public FieldMirror getDeclaredField(String name) {
        FieldMirror field = wrapped.getDeclaredField(name);
        return field != null ? vm.getFieldMirror(field) : field;
    }
    
    @Override
    public boolean isPrimitive() {
        return wrapped.isPrimitive();
    }
    
    @Override
    public List<FieldMirror> getDeclaredFields() {
        List<FieldMirror> wrappedFields = new ArrayList<FieldMirror>();
        for (FieldMirror field : wrapped.getDeclaredFields()) {
            wrappedFields.add(vm.getFieldMirror(field));
        }
        return wrappedFields;
    }
    
    @Override
    public List<ObjectMirror> getInstances() {
        final List<ObjectMirror> instances = wrapped.getInstances();
        return new AbstractList<ObjectMirror>() {
            @Override
            public ObjectMirror get(int index) {
                return (ObjectMirror)vm.getWrappedMirror(instances.get(index));
            }
            
            @Override
            public int size() {
                return instances.size();
            }
        };
    }

    @Override
    public MethodMirror getDeclaredMethod(String name, ClassMirror... paramTypes)
            throws SecurityException, NoSuchMethodException {
        
        ClassMirror[] unwrappedParamTypes = new ClassMirror[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            unwrappedParamTypes[i] = (ClassMirror)vm.unwrapMirror(paramTypes[i]);
        }
        return vm.wrapMethod(wrapped.getDeclaredMethod(name, unwrappedParamTypes));
    }
    
    @Override
    public MethodMirror getMethod(String name, ClassMirror... paramTypes)
            throws SecurityException, NoSuchMethodException {
        
        ClassMirror[] unwrappedParamTypes = new ClassMirror[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            unwrappedParamTypes[i] = (ClassMirror)vm.unwrapMirror(paramTypes[i]);
        }
        return vm.wrapMethod(wrapped.getMethod(name, unwrappedParamTypes));
    }
    
    @Override
    public ConstructorMirror getConstructor(ClassMirror... paramTypes) throws SecurityException, NoSuchMethodException {
        
        ClassMirror[] unwrappedParamTypes = new ClassMirror[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            unwrappedParamTypes[i] = (ClassMirror)vm.unwrapMirror(paramTypes[i]);
        }
        return vm.wrapConstructor(wrapped.getConstructor(unwrappedParamTypes));
    }
    
    @Override
    public List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly) {
        List<ConstructorMirror> originals = wrapped.getDeclaredConstructors(publicOnly);
        List<ConstructorMirror> result = new ArrayList<ConstructorMirror>(originals.size());
        for (ConstructorMirror original : originals) {
            result.add(vm.wrapConstructor(original));
        }
        return result;
    }
    
    @Override
    public List<MethodMirror> getDeclaredMethods(boolean publicOnly) {
        List<MethodMirror> originals = wrapped.getDeclaredMethods(publicOnly);
        List<MethodMirror> result = new ArrayList<MethodMirror>(originals.size());
        for (MethodMirror original : originals) {
            result.add(vm.wrapMethod(original));
        }
        return result;
    }
    
    @Override
    public int getModifiers() {
        return wrapped.getModifiers();
    }
    
    @Override
    public InstanceMirror newRawInstance() {
        return (InstanceMirror)vm.getWrappedMirror(wrapped.newRawInstance());
    }
    
    @Override
    public ArrayMirror newArray(int size) {
        return (ArrayMirror)vm.getWrappedMirror(wrapped.newArray(size));
    }
    
    @Override
    public ArrayMirror newArray(int... dims) {
        return (ArrayMirror)vm.getWrappedMirror(wrapped.newArray(dims));
    }
    
    @Override
    public boolean initialized() {
        return wrapped.initialized();
    }

    @Override
    public InstanceMirror getStaticFieldValues() {
        return (InstanceMirror)vm.getWrappedMirror(wrapped.getStaticFieldValues());
    }
    
    @Override
    public void bytecodeLocated(File originalBytecodeLocation) {
        wrapped.bytecodeLocated(originalBytecodeLocation);
    }
}
