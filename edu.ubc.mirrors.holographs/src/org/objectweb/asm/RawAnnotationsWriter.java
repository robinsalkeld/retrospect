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
}
