package edu.ubc.mirrors;

import java.util.List;

public interface AnnotationMirror {

    public ClassMirror getClassMirror();
    
    public List<String> getKeys();
    
    /**
     * Returns one of:
     * 
     * <ol>
     *   <li>Primitive boxed value</li>
     *   <li>String</li>
     *   <li>ClassMirror</li>
     *   <li>EnumMirror</li>
     *   <li>AnnotationMirror</li>
     *   <li>List of the above (for array values)</li>
     * </ol>
     * 
     * @param name
     * @return
     */
    public Object getValue(String name);
    
    public static class EnumMirror {
        
        public EnumMirror(ClassMirror klass, String name) {
            this.klass = klass;
            this.name = name;
        }
        private final ClassMirror klass;
        private final String name;
        
        public ClassMirror getClassMirror() {
            return klass;
        }
        
        public String getName() {
            return name;
        }
    }
}
