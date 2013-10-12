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
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.AnnotationMirror.EnumMirror;

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
        return Arrays.copyOf(out.data, out.length);
    }
    
    public static byte[] getRawBytes(List<AnnotationMirror> annotations) {
        RawAnnotationsWriter writer = new RawAnnotationsWriter(new ClassWriter(Opcodes.ASM4));
        for (AnnotationMirror a : annotations) {
            AnnotationVisitor subVisitor = writer.visitAnnotation(descForClassMirror(a.getClassMirror()), true);
            visitAnnotationMirror(subVisitor, a);
        }
        return writer.rawAnnotations();
    }
    
    private static void visitAnnotationMirror(AnnotationVisitor visitor, AnnotationMirror mirror) {
        for (String key : mirror.getKeys()) {
            Object value = mirror.getValue(key);
            visitAnnotationMirrorValue(visitor, key, value);
        }
    }
    
    private static void visitAnnotationMirrorValue(AnnotationVisitor visitor, String key, Object value) {
        if (value instanceof AnnotationMirror) {
            AnnotationMirror subAnnot = (AnnotationMirror)value;
            String desc = descForClassMirror(subAnnot.getClassMirror());
            AnnotationVisitor subVisitor = visitor.visitAnnotation(key, desc);
            visitAnnotationMirror(subVisitor, subAnnot);
        } else if (value instanceof EnumMirror) {
            EnumMirror e = (EnumMirror)value;
            String desc = Reflection.typeForClassMirror(e.getClassMirror()).getDescriptor();
            visitor.visitEnum(key, desc, e.getName());
        } else if (value instanceof EnumMirror) {
            EnumMirror e = (EnumMirror)value;
            String desc = Reflection.typeForClassMirror(e.getClassMirror()).getDescriptor();
            visitor.visitEnum(key, desc, e.getName());
        } else if (value instanceof List) {
            for (Object element : (List<?>)value) {
                visitAnnotationMirrorValue(visitor, key, element);
            }
        } else {
            visitor.visit(key, value);
        }
    }

    private static String descForClassMirror(ClassMirror klass) {
        return Reflection.typeForClassMirror(klass).getDescriptor();
    }
}
