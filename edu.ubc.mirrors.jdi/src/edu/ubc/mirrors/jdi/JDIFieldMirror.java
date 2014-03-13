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
package edu.ubc.mirrors.jdi;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;

public class JDIFieldMirror extends JDIMirror implements FieldMirror {

    final Field field;
    
    public JDIFieldMirror(JDIVirtualMachineMirror vm, Field field) {
	super(vm, field);
        this.field = field;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof JDIFieldMirror)) {
            return false;
        }
        
        JDIFieldMirror other = (JDIFieldMirror)obj;
        return field.equals(other.field) && vm.equals(other.vm);
    }
    
    @Override
    public int hashCode() {
        return 11 * field.hashCode() * vm.hashCode();
    }
    
    @Override
    public ClassMirror getDeclaringClass() {
        return vm.makeClassMirror(field.declaringType());
    }
    
    @Override
    public String getName() {
        return field.name();
    }

    @Override
    public String getTypeName() {
        return field.typeName();
    }
    
    @Override
    public ClassMirror getType() {
        try {
            return vm.makeClassMirror(field.type());
        } catch (ClassNotLoadedException e) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public int getModifiers() {
        // TODO-RS: Seems to return invalid values sometimes
//        return field.modifiers();
        throw new UnsupportedOperationException();
    }
}
