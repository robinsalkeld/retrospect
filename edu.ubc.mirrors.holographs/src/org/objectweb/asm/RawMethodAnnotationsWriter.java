/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package org.objectweb.asm;

import java.util.Arrays;
import java.util.List;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ThreadMirror;

public class RawMethodAnnotationsWriter extends MethodVisitor {

    private final ClassWriter cw;
    private final MethodWriter writer;
    private AnnotationWriter anns;
    private AnnotationWriter[] panns;
    private ByteVector annd;
    private ThreadMirror thread;
    
    public RawMethodAnnotationsWriter(ThreadMirror thread, int parameters, ClassVisitor classWriter, MethodVisitor writer) {
        super(Opcodes.ASM4);
        this.cw = (ClassWriter)classWriter;
        this.writer = (MethodWriter)writer; 
        this.panns = new AnnotationWriter[parameters];
        this.thread = thread;
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
    
    public void visitAnnotationMirrors(List<AnnotationMirror> annotations) {
        for (AnnotationMirror a : annotations) {
            AnnotationVisitor subVisitor = writer.visitAnnotation(RawAnnotationsWriter.descForClassMirror(a.getClassMirror()), true);
            RawAnnotationsWriter.visitAnnotationMirror(subVisitor, thread, a);
        }
    }
    
    public void visitParameterAnnotationMirrors(List<List<AnnotationMirror>> annotations) {
        int parameter = 0;
        for (List<AnnotationMirror> list : annotations) {
            for (AnnotationMirror a : list) {
                AnnotationVisitor subVisitor = writer.visitParameterAnnotation(parameter, RawAnnotationsWriter.descForClassMirror(a.getClassMirror()), true);
                RawAnnotationsWriter.visitAnnotationMirror(subVisitor, thread, a);
            }
            parameter++;
        }
    }
}

