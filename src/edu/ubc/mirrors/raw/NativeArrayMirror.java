package edu.ubc.mirrors.raw;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.BoxingFieldMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;

public class NativeArrayMirror implements ArrayMirror {
    
    private final Object[] array;
    
    public NativeArrayMirror(ClassMirror<?> classMirror, Object[] array) {
        this.array = array;
    }

    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        throw new IllegalStateException();
    }

    public FieldMirror getArrayElement(int index) {
        return new ElementMirror(index);
    }

    public int length() {
        return array.length;
    }

    public ClassMirror<?> getClassMirror() {
        return new NativeClassMirror<Object[]>(array.getClass());
    }
     
    private class ElementMirror extends BoxingFieldMirror {
        private final int index;
        
        public ElementMirror(int index) {
            this.index = index;
        }

        @Override
        public Object get() throws IllegalAccessException {
            return array[index];
        }

        @Override
        public void set(Object o) throws IllegalAccessException {
            array[index] = o;
        }
    }
}
