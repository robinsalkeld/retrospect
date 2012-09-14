package org.objectweb.asm;

import java.util.Arrays;

public class RawMethodAnnotationsWriter extends MethodVisitor {

    private final ClassWriter cw;
    private final MethodWriter writer;
    private AnnotationWriter anns;
    private AnnotationWriter[] panns;
    private ByteVector annd;
    
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
        this.annd = new ByteVector();
        return new AnnotationWriter(cw, false, annd, null, 0);
    }
    
    public byte[] rawAnnotations() {
        ByteVector out = new AttributeContentByteVector();
        if (anns == null) {
            out.putShort(0);
        } else {
            anns.put(out);
        }
        return Arrays.copyOf(out.data, out.length);
    }
    
    public byte[] rawParameterAnnotations() {
        ByteVector out = new AttributeContentByteVector();
        AnnotationWriter.put(panns, 0, out);
        return Arrays.copyOf(out.data, out.length);
    }
    
    public byte[] rawAnnotationDefault() {
        if (annd == null) {
            return null;
        } else {
            return Arrays.copyOf(annd.data, annd.length);
        }
    }
}

