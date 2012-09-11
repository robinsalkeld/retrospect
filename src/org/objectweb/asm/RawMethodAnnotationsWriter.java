package org.objectweb.asm;

public class RawMethodAnnotationsWriter extends MethodVisitor {

    private final ClassWriter cw;
    private final MethodWriter writer;
    private AnnotationWriter anns;
    private AnnotationWriter[] panns;
    private ByteVector annd = new ByteVector();
    
    public RawMethodAnnotationsWriter(int parameters, ClassVisitor classWriter, MethodVisitor writer) {
        super(Opcodes.ASM4);
        this.cw = (ClassWriter)classWriter;
        this.writer = (MethodWriter)writer; 
        this.panns = new AnnotationWriter[parameters];
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationWriter av = (AnnotationWriter)writer.visitAnnotation(desc, visible);
        if (visible) {
            anns = av;
        }
        return av;
    }
    
    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
        AnnotationWriter av = (AnnotationWriter)writer.visitParameterAnnotation(parameter, desc, visible);
        if (visible) {
            panns[parameter] = av;
        }
        return av;
    }
    
    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return new AnnotationWriter(cw, false, annd, null, 0);
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
    
    public byte[] rawParameterAnnotations() {
        ByteVector out = new AttributeContentByteVector();
        AnnotationWriter.put(panns, 0, out);
        return out.data;
    }
    
    public byte[] rawAnnotationDefault() {
        return annd.data;
    }
}

