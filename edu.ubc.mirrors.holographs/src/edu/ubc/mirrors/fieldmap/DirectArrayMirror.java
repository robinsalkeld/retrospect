package edu.ubc.mirrors.fieldmap;

import java.util.ArrayList;
import java.util.List;

import edu.ubc.mirrors.BoxingArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

public class DirectArrayMirror extends BoxingArrayMirror implements ObjectArrayMirror {

    private final ClassMirror classMirror;
    private final Object[] array;
    
    public DirectArrayMirror(ClassMirror classMirror, int length) {
        this.classMirror = classMirror;
        this.array = new Object[length];
    }
    
    public DirectArrayMirror(ClassMirror classMirror, int[] dims) {
        this(classMirror, dimsList(dims));
    }
    
    private static List<Integer> dimsList(int[] dims) {
        List<Integer> result = new ArrayList<Integer>(dims.length);
        for (int dim : dims) {
            result.add(dim);
        }
        return result;
    }
    
    private DirectArrayMirror(ClassMirror classMirror, List<Integer> dims) {
        this.classMirror = classMirror;
        this.array = new Object[dims.get(0)];
        
        if (dims.size() > 1) {
            ClassMirror componentClassMirror = classMirror.getComponentClassMirror();
            List<Integer> componentDims = dims.subList(1, dims.size());
            for (int i = 0; i < array.length; i++) {
                this.array[i] = new DirectArrayMirror(componentClassMirror, componentDims);
            }
        }
    }
    
    public int length() {
        return array.length;
    }

    public ClassMirror getClassMirror() {
        return classMirror;
    }

    @Override
    public int identityHashCode() {
        return hashCode();
    }
    
    public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
        return (ObjectMirror)array[index];
    }

    public void set(int index, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        array[index] = o;
    }

    @Override
    protected Object getBoxedValue(int index) throws ArrayIndexOutOfBoundsException {
        Object result = array[index];
        if (result != null) {
            return result;
        } else if (classMirror.getComponentClassMirror().isPrimitive()) {
            // Return the appropriate primitive default
            String elementName = classMirror.getComponentClassMirror().getClassName();
            if (elementName.equals("boolean")) {
                return Boolean.FALSE;
            } else if (elementName.equals("byte")) {
                return Byte.valueOf((byte)0);
            } else if (elementName.equals("char")) {
                return Character.valueOf((char)0);
            } else if (elementName.equals("short")) {
                return Short.valueOf((short)0);
            } else if (elementName.equals("int")) {
                return Integer.valueOf(0);
            } else if (elementName.equals("long")) {
                return Long.valueOf(0);
            } else if (elementName.equals("float")) {
                return Float.valueOf((float)0.0);
            } else if (elementName.equals("double")) {
                return Double.valueOf(0.0);
            } else {
                throw new IllegalStateException();
            }
        } else {
            return result;
        }
    }

    @Override
    protected void setBoxedValue(int index, Object o) throws ArrayIndexOutOfBoundsException {
        array[index] = o;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + classMirror.getClassName();
    }
}
