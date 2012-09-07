package org.objectweb.asm;

public class RawAnnotationsWriter extends ClassVisitor {

    private final ClassWriter writer;
    private AnnotationWriter anns;
    
    public RawAnnotationsWriter(ClassReader reader) {
        super(Opcodes.ASM4);
        // The reader is provided so the constant pool will be pre-populated
        this.writer = new ClassWriter(reader, Opcodes.ASM4); 
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationWriter av = (AnnotationWriter)writer.visitAnnotation(desc, visible);
        if (visible) {
            anns = av;
        }
        return av;
    }
    
    public byte[] toByteArray() {
        ByteVector out = new ByteVector() {
            boolean first = true;
            @Override
            public ByteVector putInt(int i) {
                // Skip the first int, which is the total size of
                // the annotations and isn't included in this format.
                if (first) {
                    first = false;
                } else {
                    super.putInt(i);
                }
                return this;
            }
        };
        if (anns == null) {
            out.putShort(0);
        } else {
            anns.put(out);
        }
        return out.data;
    }
}
