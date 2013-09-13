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
package edu.ubc.mirrors.holographs.jdi;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;

public class HolographField extends Holograph implements Field {

    public HolographField(JDIHolographVirtualMachine vm, ReferenceType declaringType, String name) {
        super(vm, vm.mirrorOf(name));
        this.declaringType = declaringType;
        this.name = name;
    }

    private final ReferenceType declaringType;
    private final String name;
    
    @Override
    public ReferenceType declaringType() {
        return declaringType;
    }

    @Override
    public String genericSignature() {
        return null;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean isSynthetic() {
        return false;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String signature() {
        return null;
    }

    @Override
    public boolean isPackagePrivate() {
        return false;
    }

    @Override
    public boolean isPrivate() {
        return false;
    }

    @Override
    public boolean isProtected() {
        return false;
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public int modifiers() {
        return 0;
    }

    @Override
    public int compareTo(Field o) {
        return name.compareTo(((HolographField)o).name);
    }

    @Override
    public boolean isEnumConstant() {
        return false;
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public boolean isVolatile() {
        return false;
    }

    @Override
    public Type type() throws ClassNotLoadedException {
        return null;
    }

    @Override
    public String typeName() {
        return null;
    }

}
