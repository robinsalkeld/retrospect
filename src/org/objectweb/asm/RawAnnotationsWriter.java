package org.objectweb.asm;

public class RawAnnotationsWriter extends ClassVisitor {

    private final ClassWriter writer;
    private AnnotationWriter anns;
    
    public RawAnnotationsWriter(ClassWriter writer) {
        super(Opcodes.ASM4);
        // The writer is provided so the constant pool will be pre-populated
        this.writer = writer; 
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationWriter av = (AnnotationWriter)writer.visitAnnotation(desc, visible);
        if (visible) {
            anns = av;
        }
        return av;
    }
    
    public byte[] rawAnnotations() {
        ByteVector out = new AttributeContentByteVector();
        if (anns == null) {
            out.putShort(0);
        } else {
            anns.put(out);
        }
        return out.data;
    }
}
