package edu.ubc.mirrors.eclipse.mat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IInstance;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.raw.BytecodeClassMirror;

public class HeapDumpClassMirror extends BytecodeClassMirror {

    private final HeapDumpClassMirrorLoader loader;
    private final IClass klass;
    
    
    public HeapDumpClassMirror(HeapDumpClassMirrorLoader loader, IClass klass) {
        super(getClassName(klass));
        this.loader = loader;
        this.klass = klass;
    }
    
    @Override
    public VirtualMachineMirror getVM() {
        return loader.getVM();
    }
    
    @Override
    public byte[] getBytecode() {
        String className = getClassName(klass);
        return loader.getBytecodeLoader().findLoadedClassMirror(className).getBytecode();
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
                return new HeapDumpFieldMirror(loader, field);
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
        return loader;
    }

    public List<ObjectMirror> getInstances() throws SnapshotException {
        List<ObjectMirror> result = new ArrayList<ObjectMirror>();
        for (int id : klass.getObjectIds()) {
            IInstance object = (IInstance)klass.getSnapshot().getObject(id);
            result.add(HeapDumpInstanceMirror.makeMirror(loader, object));
        }
        return result;
    }
    
}
