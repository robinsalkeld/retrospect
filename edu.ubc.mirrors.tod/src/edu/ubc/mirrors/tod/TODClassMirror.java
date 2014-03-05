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
package edu.ubc.mirrors.tod;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.event.ICreationEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.ITargetEvent;
import tod.core.database.structure.BehaviorKind;
import tod.core.database.structure.IArrayTypeInfo;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IPrimitiveTypeInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.BlankInstanceMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.StaticFieldValuesMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class TODClassMirror extends BlankInstanceMirror implements ClassMirror {

    private final TODVirtualMachineMirror vm;
    protected final ITypeInfo classInfo;

    public TODClassMirror(TODVirtualMachineMirror vm, ITypeInfo classInfo) {
        this.vm = vm;
        this.classInfo = classInfo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TODClassMirror)) {
            return false;
        }
        
        return classInfo.equals(((TODClassMirror)obj).classInfo);
    }

    @Override
    public int hashCode() {
        return 19 * classInfo.hashCode();
    }
    
    @Override
    public ClassMirror getClassMirror() {
        return vm.findBootstrapClassMirror(Class.class.getName());
    }

    @Override
    public VirtualMachineMirror getVM() {
        return vm;
    }

    @Override
    public String getClassName() {
        return classInfo.getName();
    }

    @Override
    public String getSignature() {
        return classInfo.getName();
    }

    @Override
    public ClassMirrorLoader getLoader() {
        // TODO-RS: The TOD database doesn't record this. Will it work to
        // just approximate by pretending all classes are bootstrap classes?
        return null;
    }

    @Override
    public byte[] getBytecode() {
        if (classInfo instanceof IClassInfo) {
            // Note that TOD seems to pull standard library bytecode from the client
            // JVM instead of the trace, so be careful that the TODVirtualMachineMirror
            // runs on the right JVM or this doesn't work!
            return ((IClassInfo)classInfo).getOriginalBytecode(); 
        } else {
            return null;
        }
    }

    @Override
    public boolean isPrimitive() {
        return classInfo.isPrimitive();
    }

    @Override
    public boolean isArray() {
        return classInfo.isArray();
    }

    @Override
    public ClassMirror getComponentClassMirror() {
        if (classInfo instanceof IArrayTypeInfo) {
            return vm.makeClassMirror(((IArrayTypeInfo)classInfo).getElementType());
        } else {
            return null;
        }
    }

    @Override
    public ClassMirror getSuperClassMirror() {
        if (classInfo instanceof IClassInfo) {
            ITypeInfo supertype = ((IClassInfo)classInfo).getSupertype();
            if (supertype == null && !classInfo.getName().equals(Object.class.getName())) {
                return vm.findBootstrapClassMirror(Object.class.getName());
            } else {
                return vm.makeClassMirror(supertype);
            }
        } else if (classInfo instanceof IPrimitiveTypeInfo) {
            return null;
        } else /* array */ {
            return vm.findBootstrapClassMirror(Object.class.getName());
        }
    }

    @Override
    public boolean isInterface() {
        if (classInfo instanceof IClassInfo) {
            return ((IClassInfo)classInfo).isInterface();
        } else {
            return false;
        }
    }

    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        if (classInfo instanceof IClassInfo) {
            return vm.makeClassMirrorList(((IClassInfo)classInfo).getInterfaces());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public FieldMirror getDeclaredField(String name) {
        if (classInfo instanceof IClassInfo) {
            IFieldInfo field = ((IClassInfo)classInfo).getField(name);
            return field != null ? new TODFieldMirror(vm, field) : null;
        } else {
            return null;
        }
        
    }

    @Override
    public List<FieldMirror> getDeclaredFields() {
        if (classInfo instanceof IClassInfo) {
            List<FieldMirror> result = new ArrayList<FieldMirror>();
            for (IFieldInfo field : ((IClassInfo)classInfo).getFields()) {
                result.add(new TODFieldMirror(vm, field));
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<ObjectMirror> getInstances() {
        List<ObjectMirror> result = new ArrayList<ObjectMirror>();
        
        // TODO-RS: This is ideal but doesn't work well in practice since lots of instantiations
        // won't appear in the log at all.
//        IEventFilter filter = vm.getLogBrowser().createInstantiationsFilter(classInfo);
//        for (ILogEvent event : vm.allEvents(filter)) {
//            ICreationEvent creationEvent = (ICreationEvent)event;
//            ObjectId objectId = (ObjectId)creationEvent.getInstance();
//            result.add(vm.makeMirror(objectId));
//        }

        for (ILogEvent event : vm.allEvents()) {
            if (event instanceof ITargetEvent) {
                ITargetEvent targetEvent = (ITargetEvent)event;
                ObjectId objectId = (ObjectId)targetEvent.getTarget();
                if (objectId != null) {
                    IObjectInspector browser = vm.getLogBrowser().createObjectInspector(objectId);
                    if (browser.getType().equals(classInfo)) {
                        result.add(vm.makeMirror(browser));
                    }
                }
            }
        }
        
        return result;
    }

    @Override
    public List<ClassMirror> getSubclassMirrors() {
        // TODO-RS: Obviously very slow - this should be cached incrementally
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        for (ClassMirror other : vm.findAllClasses()) {
            if (equals(other.getSuperClassMirror())) {
                result.add(other);
            }
        }
        return result;
    }

    @Override
    public MethodMirror getDeclaredMethod(String name, String... paramTypeNames)
            throws SecurityException, NoSuchMethodException {

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

        List<String> paramTypeNamesList = Arrays.asList(paramTypeNames);
        
        for (ConstructorMirror cMirror : getDeclaredConstructors(false)) {
            if (cMirror.getParameterTypeNames().equals(paramTypeNamesList)) {
                return cMirror;
            }
        }
        return null;
    }

    @Override
    public List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly) {
        if (classInfo instanceof IClassInfo) {
            IClassInfo ci = (IClassInfo)classInfo;
            List<ConstructorMirror> result = new ArrayList<ConstructorMirror>();
            for (IBehaviorInfo behavior : ci.getBehaviors()) {
                if (behavior.getBehaviourKind() == BehaviorKind.CONSTRUCTOR) {
                    result.add(vm.makeConstructorMirror(behavior));
                }
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<MethodMirror> getDeclaredMethods(boolean publicOnly) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getModifiers() {
        throw new UnsupportedOperationException();
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
        // Only initialized classes show up in the trace.
        return true;
    }

    @Override
    public StaticFieldValuesMirror getStaticFieldValues() {
        if (classInfo instanceof IClassInfo) {
            return new TODStaticFieldValuesMirror(vm, (IClassInfo)classInfo);
        } else {
            return null;
        }
        
    }

    @Override
    public void bytecodeLocated(File originalBytecodeLocation) {
        
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
    public FieldMirror createField(int modifiers, ClassMirror type, String name) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + classInfo;
    }
}
