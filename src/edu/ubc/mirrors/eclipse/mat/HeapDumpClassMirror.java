package edu.ubc.mirrors.eclipse.mat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IClassLoader;
import org.eclipse.mat.snapshot.model.IInstance;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.raw.BytecodeClassMirror;

public class HeapDumpClassMirror extends BytecodeClassMirror {

    private final HeapDumpVirtualMachineMirror vm;
    private final IClass klass;
    private final IClassLoader loader;
    
    
    public HeapDumpClassMirror(HeapDumpVirtualMachineMirror vm, IClass klass) {
        super(getClassName(klass));
        this.vm = vm;
        this.klass = klass;
        this.loader = getClassLoader(klass);
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
    public VirtualMachineMirror getVM() {
        return vm;
    }
    
    @Override
    public byte[] getBytecode() {
        ClassMirror classMirror = vm.getBytecodeClassMirror(this);
        return classMirror.getBytecode();
    }
    
    public static String arrayElementDescriptor(String name) {
        if (name.equals("boolean")) {
            return "Z";
        } else if (name.equals("byte")) {
            return "B";
        } else if (name.equals("char")) {
            return "C";
        } else if (name.equals("short")) {
            return "S";
        } else if (name.equals("int")) {
            return "I";
        } else if (name.equals("long")) {
            return "J";
        } else if (name.equals("float")) {
            return "F";
        } else if (name.equals("double")) {
            return "D";
        } else {
            return "L" + name + ";";
        }
    }
    
    public static String getClassName(IClass klass) {
        String name = klass.getName();
        if (name.endsWith("[]")) {
            String elementName = name;
            String dimsString = "";
            while (elementName.endsWith("[]")) {
                elementName = elementName.substring(0, elementName.length() - 2);
                dimsString += "[";
            }
            name = dimsString + arrayElementDescriptor(elementName);
        }
        return name;
    }
    
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        List<Field> fields = klass.getStaticFields();
        for (Field field : fields) {
            if (field.getName().equals(name)) {
                return new HeapDumpFieldMirror(vm, field);
            }
        }
        throw new NoSuchFieldException(name);
    }

    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        // The MAT model doesn't expose member fields for classes, so grab them
        // from the native super implementation.
        return super.getMemberField(name);
    }
    
    @Override
    public HeapDumpClassMirrorLoader getLoader() {
        return (HeapDumpClassMirrorLoader)vm.makeMirror(loader);
    }

    public List<InstanceMirror> getInstances() {
        List<InstanceMirror> result = new ArrayList<InstanceMirror>();
        try {
            for (int id : klass.getObjectIds()) {
                IInstance object = (IInstance)klass.getSnapshot().getObject(id);
                result.add((InstanceMirror)vm.makeMirror(object));
            }
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    
}
