/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package edu.ubc.mirrors.eclipse.mat;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.FieldDescriptor;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IClassLoader;
import org.eclipse.mat.snapshot.model.IObject;
import org.objectweb.asm.Type;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.BoxingInstanceMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorLocation;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.StaticFieldValuesMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.holograms.HologramClassGenerator;

public class HeapDumpClassMirror extends BoxingInstanceMirror implements ClassMirror, HeapDumpObjectMirror {

    private final HeapDumpVirtualMachineMirror vm;
    protected final IClass klass;
    private final IClassLoader loader;
    private Map<String, HeapDumpFieldMirror> fieldMirrors;
    private Map<FieldMirror, Integer> instanceFieldOffsets;
    
    public HeapDumpClassMirror(HeapDumpVirtualMachineMirror vm, IClass klass) {
        if (klass == null) {
            throw new NullPointerException();
        }
        this.vm = vm;
        this.klass = klass;
        this.loader = getClassLoader(klass);
        this.staticFieldValues = new HeapDumpClassStaticValues(this);
    }
    
    @Override
    public boolean equals(Object obj) {
        return HeapDumpVirtualMachineMirror.equalObjects(this, obj);
    }
    
    @Override
    public int hashCode() {
        return 11 + klass.hashCode();
    }
    
    public static IClassLoader getClassLoader(IClass klass) {
        int loaderID = klass.getClassLoaderId();
        if (loaderID == 0) {
            return null;
        } else {
            try {
                return (IClassLoader)klass.getSnapshot().getObject(loaderID);
            } catch (SnapshotException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    @Override
    public HeapDumpVirtualMachineMirror getVM() {
        return vm;
    }
    
    @Override
    public byte[] getBytecode() {
        byte[] result = vm.locateBytecode(this);
        if (result != null) {
            return result;
        }
        
        throw new UnsupportedOperationException();
    }
    
    private void resolveFields() {
        if (fieldMirrors == null) {
            fieldMirrors = new LinkedHashMap<String, HeapDumpFieldMirror>();
            
            List<Field> fields = klass.getStaticFields();
            for (Field field : fields) {
                    fieldMirrors.put(field.getName(), new HeapDumpFieldMirror(this, field));
            }
            
            for (FieldDescriptor fd : klass.getFieldDescriptors()) {
                fieldMirrors.put(fd.getName(), new HeapDumpFieldMirror(this, fd));
            }
        }
    }
    
    @Override
    public FieldMirror getDeclaredField(final String name) {
        resolveFields();
        return fieldMirrors.get(name);
    }
    
    int getFieldOffset(FieldMirror field) {
        resolveFieldOffsets();
        return instanceFieldOffsets.get(field);
    }
    
    private void resolveFieldOffsets() {
        if (instanceFieldOffsets == null) {
            instanceFieldOffsets = new HashMap<FieldMirror, Integer>();
            
            ClassMirror klass = this;
            int offset = 0;
            while (klass != null) {
                for (FieldMirror field : klass.getDeclaredFields()) {
                    if (!((HeapDumpFieldMirror)field).isStatic()) {
                        instanceFieldOffsets.put(field, offset++);
                    }
                }
                klass = klass.getSuperClassMirror();
            }
        }
    }

    @Override
    public HeapDumpClassMirrorLoader getLoader() {
        return (HeapDumpClassMirrorLoader)vm.makeMirror(loader);
    }

    public List<ObjectMirror> getInstances() {
        List<ObjectMirror> result = new ArrayList<ObjectMirror>();
        try {
            // TODO-RS: Handle interfaces as well
            addDirectInstances(result, klass);
//            for (IClass subclass : klass.getAllSubclasses()) {
//                addDirectInstances(result, subclass);
//            }
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public List<ClassMirror> getSubclassMirrors() {
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        for (IClass subclass : klass.getAllSubclasses()) {
            result.add(vm.makeClassMirror(subclass));
        }
        return result;
    }
    
    private void addDirectInstances(List<ObjectMirror> list, IClass klass) throws SnapshotException {
        for (int id : klass.getObjectIds()) {
            IObject object = (IObject)klass.getSnapshot().getObject(id);
            ObjectMirror mirror = vm.makeMirror(object);
            if (mirror != null) {
                list.add(mirror);
            }
        }
    }
    
    @Override
    public HeapDumpClassMirror getClassMirror() {
        return vm.findBootstrapClassMirror(Class.class.getName());
    }

    @Override
    public String getClassName() {
        return klass.getName();
    }

    @Override
    public String getSignature() {
        return Reflection.typeForClassMirror(this).getDescriptor();
    }
    
    @Override
    public boolean isPrimitive() {
        // Primitive classes don't show up in the dump
        return false;
    }

    @Override
    public boolean isArray() {
        return klass.isArrayType();
    }

    @Override
    public ClassMirror getComponentClassMirror() {
        String name = getClassName();
        if (name.endsWith("[]")) {
            Type componentType = Reflection.typeForClassName(name.substring(0, name.length() - 2));
            if (HologramClassGenerator.isRefType(componentType)) {
                String componentClassName = componentType.getInternalName().replace('/', '.');
                if (loader == null) {
                    return vm.findBootstrapClassMirror(componentClassName);
                } else {
                    return getLoader().findLoadedClassMirror(componentClassName);    
                }
            } else {
                return vm.getPrimitiveClass(componentType.getClassName());
            }
        } else {
            return null;
        }
    }

    @Override
    public ClassMirror getSuperClassMirror() {
        return vm.makeClassMirror(klass.getSuperClass());
    }

    @Override
    public boolean isInterface() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FieldMirror> getDeclaredFields() {
        resolveFields();
        return new ArrayList<FieldMirror>(fieldMirrors.values());
    }

    @Override
    public MethodMirror getDeclaredMethod(String name,
            String... paramTypeNames) throws SecurityException,
            NoSuchMethodException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public MethodMirror getMethod(String name, String... paramTypeNames)
            throws SecurityException, NoSuchMethodException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ConstructorMirror getConstructor(String... paramTypeNames)
            throws SecurityException, NoSuchMethodException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MethodMirror> getDeclaredMethods(boolean publicOnly) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int getModifiers() {
        // TODO-RS: Let's pretend for now
        return Modifier.PUBLIC;
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
    public boolean initialized() {
        // Unfortunately the model doesn't track this
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + klass;
    }
    
    private final StaticFieldValuesMirror staticFieldValues;
    
    @Override
    public StaticFieldValuesMirror getStaticFieldValues() {
        return staticFieldValues;
    }

    @Override
    public Object getBoxedValue(FieldMirror field) throws IllegalAccessException {
        // The MAT model doesn't expose member fields for classes.
        // All the fields on Class are caches though, so it's safe to start off null/0
        return null;
    }

    @Override
    public void setBoxedValue(FieldMirror field, Object o) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int identityHashCode() {
        return vm.identityHashCode(klass);
    }

    public void bytecodeLocated(File originalBytecodeLocation) {
        vm.bytecodeLocated(this, originalBytecodeLocation);
    }

    @Override
    public IObject getHeapDumpObject() {
        return klass;
    }
    
    @Override
    public ClassMirror getEnclosingClassMirror() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public MethodMirror getEnclosingMethodMirror() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public List<AnnotationMirror> getAnnotations(ThreadMirror thread) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public MirrorLocation locationOfLine(int lineNumber) {
        throw new UnsupportedOperationException();
    }
    
    public FieldMirror createField(int modifiers, ClassMirror type, String name) {
        throw new UnsupportedOperationException();
    }
}
