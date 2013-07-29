package edu.ubc.mirrors.asjdi;

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.asjdi.MirrorsMirrorWithModifiers;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class ConstructorMirrorMethod extends MirrorsMirrorWithModifiers implements Method {

    protected final ConstructorMirror wrapped;
    
    public ConstructorMirrorMethod(MirrorsVirtualMachine vm, ConstructorMirror wrapped) {
        super(vm, wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public ReferenceType declaringType() {
        return (ReferenceType)vm.typeForClassMirror(wrapped.getDeclaringClass());
    }

    @Override
    public String genericSignature() {
        return null;
    }

    @Override
    public String signature() {
        return wrapped.getSignature();
    }

    @Override
    public int modifiers() {
        return wrapped.getModifiers();
    }

    @Override
    public int compareTo(Method o) {
        return signature().compareTo(o.signature());
    }

    @Override
    public List<Location> allLineLocations() throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public List<Location> allLineLocations(String arg0, String arg1) throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public List<String> argumentTypeNames() {
        return wrapped.getParameterTypeNames();
    }

    @Override
    public List<Type> argumentTypes() throws ClassNotLoadedException {
        List<Type> result = new ArrayList<Type>();
        for (ClassMirror argType : wrapped.getParameterTypes()) {
            result.add(vm.typeForClassMirror(argType));
        }
        return result;
    }

    @Override
    public List<LocalVariable> arguments() throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public byte[] bytecodes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isConstructor() {
        return true;
    }

    @Override
    public boolean isStaticInitializer() {
        return false;
    }

    @Override
    public Location location() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location locationOfCodeIndex(long arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Location> locationsOfLine(int arg0) throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public List<Location> locationsOfLine(String arg0, String arg1, int arg2)
            throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public List<LocalVariable> variables() throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public List<LocalVariable> variablesByName(String arg0) throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public boolean isObsolete() {
        return false;
    }

    @Override
    public String name() {
        return "<init>";
    }

    @Override
    public Type returnType() throws ClassNotLoadedException {
        return declaringType();
    }

    @Override
    public String returnTypeName() {
        return declaringType().name();
    }
}