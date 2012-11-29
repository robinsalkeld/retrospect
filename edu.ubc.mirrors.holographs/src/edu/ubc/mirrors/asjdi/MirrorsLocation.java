package edu.ubc.mirrors.asjdi;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;

public class MirrorsLocation implements Location {

    private final MirrorsVirtualMachine vm;
    public MirrorsLocation(MirrorsVirtualMachine vm,
            ClassMirror declaringClass, String fileName,
            int lineNumber) {
        this.vm = vm;
        this.declaringClass = declaringClass;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }

    private final ClassMirror declaringClass;
    private final String fileName;
    private int lineNumber;
    
    @Override
    public VirtualMachine virtualMachine() {
        return vm;
    }
    
    @Override
    public int compareTo(Location o) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public long codeIndex() {
        return -1;
    }
    
    @Override
    public ReferenceType declaringType() {
        return (ReferenceType)vm.typeForClassMirror(declaringClass);
    }
    
    @Override
    public int lineNumber() {
        return lineNumber;
    }
    
    @Override
    public int lineNumber(String arg0) {
        return lineNumber;
    }
    
    @Override
    public Method method() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String sourceName() throws AbsentInformationException {
        return fileName;
    }
    
    @Override
    public String sourceName(String arg0) throws AbsentInformationException {
        return fileName;
    }
    
    @Override
    public String sourcePath() throws AbsentInformationException {
        throw new AbsentInformationException();
    }
    
    @Override
    public String sourcePath(String arg0) throws AbsentInformationException {
        throw new AbsentInformationException();
    }
    
    public static Location locationForMethod(final MirrorsVirtualMachine vm, final MethodMirror method) {
        return new MirrorsLocation(vm, method.getDeclaringClass(), method.getDeclaringClass().getClassName().replace('.', '/') + ".java", -1) {
            @Override
            public Method method() {
                return new MethodMirrorMethod(vm, method);
            }
        };
    }
}
