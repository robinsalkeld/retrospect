package edu.ubc.mirrors.holographs.jdi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Field;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;

import edu.ubc.mirrors.MethodHandle;
import edu.ubc.mirrors.holograms.HologramClassGenerator;

public class HolographReferenceType extends Holograph implements ReferenceType {

    final ReferenceType wrapped;
    
    /**
     * @param o
     * @return
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ReferenceType o) {
        return wrapped.compareTo(((HolographReferenceType)o).wrapped);
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#allFields()
     */
    public List<Field> allFields() {
        ObjectReference classMirror = getClassMirror();
        ObjectReference fieldsMap = (ObjectReference)vm.invokeMethodHandle(classMirror, MethodHandle.CLASS_MIRROR_GET_DECLARED_FIELDS);
        
        final Map map = null;
        ObjectReference fieldNames = (ObjectReference)vm.invokeMethodHandle(fieldsMap, new MethodHandle() {
            protected void methodCall() throws Throwable {
                map.keySet();
            }
        });
        final Set set = null;
        int size = ((IntegerValue)vm.invokeMethodHandle(fieldNames, new MethodHandle() {
            protected void methodCall() {
                set.size();
            }
        })).intValue();
        final Iterator iter = null;
        List<Field> fields = new ArrayList<Field>();
        fields.add(new HolographField(vm, this, "<mirror>"));
        MethodHandle nextMethod = new MethodHandle() {
            protected void methodCall() {
                iter.next();
            }  
        };
        ObjectReference iterRef = (ObjectReference)vm.invokeMethodHandle(fieldNames, new MethodHandle() {
            protected void methodCall() throws Throwable {
                set.iterator();
            }
        });
        for (int i = 0; i < size; i++) {
            StringReference name = (StringReference)vm.invokeMethodHandle(iterRef, nextMethod);
            Field field = new HolographField(vm, this, name.value());
            fields.add(field);
        }
        return fields;
    }

    public ObjectReference getClassMirror() {
        ClassLoaderReference loader = wrapped.classLoader();
        StringReference hologramClassName = vm.mirrorOf(name());
        return (ObjectReference)vm.invokeMethodHandle(loader, MethodHandle.HOLOGRAM_CLASS_LOADER_LOAD_ORIGINAL_CLASS_MIRROR, hologramClassName);
    }
    
    /**
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.ReferenceType#allLineLocations()
     */
    public List<Location> allLineLocations() throws AbsentInformationException {
        return wrapped.allLineLocations();
    }

    /**
     * @param arg0
     * @param arg1
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.ReferenceType#allLineLocations(java.lang.String, java.lang.String)
     */
    public List<Location> allLineLocations(String arg0, String arg1)
            throws AbsentInformationException {
        return wrapped.allLineLocations(arg0, arg1);
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#allMethods()
     */
    public List<Method> allMethods() {
        return wrapped.allMethods();
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#availableStrata()
     */
    public List<String> availableStrata() {
        return wrapped.availableStrata();
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#classLoader()
     */
    public ClassLoaderReference classLoader() {
        return wrapped.classLoader();
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#classObject()
     */
    public ClassObjectReference classObject() {
        return wrapped.classObject();
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#constantPool()
     */
    public byte[] constantPool() {
        return wrapped.constantPool();
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#constantPoolCount()
     */
    public int constantPoolCount() {
        return wrapped.constantPoolCount();
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#defaultStratum()
     */
    public String defaultStratum() {
        return wrapped.defaultStratum();
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#failedToInitialize()
     */
    public boolean failedToInitialize() {
        return wrapped.failedToInitialize();
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.ReferenceType#fieldByName(java.lang.String)
     */
    public Field fieldByName(String arg0) {
        return wrapped.fieldByName(arg0);
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#fields()
     */
    public List<Field> fields() {
        return wrapped.fields();
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#genericSignature()
     */
    public String genericSignature() {
        return wrapped.genericSignature();
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.ReferenceType#getValue(com.sun.jdi.Field)
     */
    public Value getValue(Field arg0) {
        return wrapped.getValue(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.ReferenceType#getValues(java.util.List)
     */
    public Map<Field, Value> getValues(List arg0) {
        return wrapped.getValues(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.ReferenceType#instances(long)
     */
    public List<ObjectReference> instances(long arg0) {
        return wrapped.instances(arg0);
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#isAbstract()
     */
    public boolean isAbstract() {
        return wrapped.isAbstract();
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#isFinal()
     */
    public boolean isFinal() {
        return wrapped.isFinal();
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#isInitialized()
     */
    public boolean isInitialized() {
        return wrapped.isInitialized();
    }

    /**
     * @return
     * @see com.sun.jdi.Accessible#isPackagePrivate()
     */
    public boolean isPackagePrivate() {
        return wrapped.isPackagePrivate();
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#isPrepared()
     */
    public boolean isPrepared() {
        return wrapped.isPrepared();
    }

    /**
     * @return
     * @see com.sun.jdi.Accessible#isPrivate()
     */
    public boolean isPrivate() {
        return wrapped.isPrivate();
    }

    /**
     * @return
     * @see com.sun.jdi.Accessible#isProtected()
     */
    public boolean isProtected() {
        return wrapped.isProtected();
    }

    /**
     * @return
     * @see com.sun.jdi.Accessible#isPublic()
     */
    public boolean isPublic() {
        return wrapped.isPublic();
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#isStatic()
     */
    public boolean isStatic() {
        return wrapped.isStatic();
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#isVerified()
     */
    public boolean isVerified() {
        return wrapped.isVerified();
    }

    /**
     * @param arg0
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.ReferenceType#locationsOfLine(int)
     */
    public List<Location> locationsOfLine(int arg0)
            throws AbsentInformationException {
        return wrapped.locationsOfLine(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.ReferenceType#locationsOfLine(java.lang.String, java.lang.String, int)
     */
    public List<Location> locationsOfLine(String arg0, String arg1, int arg2)
            throws AbsentInformationException {
        return wrapped.locationsOfLine(arg0, arg1, arg2);
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#majorVersion()
     */
    public int majorVersion() {
        return wrapped.majorVersion();
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#methods()
     */
    public List<Method> methods() {
        return wrapped.methods();
    }

    /**
     * @param arg0
     * @param arg1
     * @return
     * @see com.sun.jdi.ReferenceType#methodsByName(java.lang.String, java.lang.String)
     */
    public List<Method> methodsByName(String arg0, String arg1) {
        return wrapped.methodsByName(arg0, arg1);
    }

    /**
     * @param arg0
     * @return
     * @see com.sun.jdi.ReferenceType#methodsByName(java.lang.String)
     */
    public List<Method> methodsByName(String arg0) {
        return wrapped.methodsByName(arg0);
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#minorVersion()
     */
    public int minorVersion() {
        return wrapped.minorVersion();
    }

    /**
     * @return
     * @see com.sun.jdi.Accessible#modifiers()
     */
    public int modifiers() {
        return wrapped.modifiers();
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#name()
     */
    public String name() {
        return HologramClassGenerator.getOriginalBinaryClassName(wrapped.name());
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#nestedTypes()
     */
    public List<ReferenceType> nestedTypes() {
        return wrapped.nestedTypes();
    }

    /**
     * @return
     * @see com.sun.jdi.Type#signature()
     */
    public String signature() {
        Type type = Type.getType(wrapped.signature());
        return HologramClassGenerator.getOriginalType(type).getDescriptor();
    }

    /**
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.ReferenceType#sourceDebugExtension()
     */
    public String sourceDebugExtension() throws AbsentInformationException {
        return wrapped.sourceDebugExtension();
    }

    /**
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.ReferenceType#sourceName()
     */
    public String sourceName() throws AbsentInformationException {
        return wrapped.sourceName();
    }

    /**
     * @param arg0
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.ReferenceType#sourceNames(java.lang.String)
     */
    public List<String> sourceNames(String arg0)
            throws AbsentInformationException {
        return wrapped.sourceNames(arg0);
    }

    /**
     * @param arg0
     * @return
     * @throws AbsentInformationException
     * @see com.sun.jdi.ReferenceType#sourcePaths(java.lang.String)
     */
    public List<String> sourcePaths(String arg0)
            throws AbsentInformationException {
        return wrapped.sourcePaths(arg0);
    }

    /**
     * @return
     * @see com.sun.jdi.Mirror#toString()
     */
    public String toString() {
        return wrapped.toString();
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#visibleFields()
     */
    public List<Field> visibleFields() {
        return wrapped.visibleFields();
    }

    /**
     * @return
     * @see com.sun.jdi.ReferenceType#visibleMethods()
     */
    public List<Method> visibleMethods() {
        return wrapped.visibleMethods();
    }

    public HolographReferenceType(JDIHolographVirtualMachine vm, ReferenceType wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }


}
