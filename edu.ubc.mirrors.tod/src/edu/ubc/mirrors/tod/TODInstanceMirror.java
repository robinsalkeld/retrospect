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
package edu.ubc.mirrors.tod;

import tod.core.database.browser.IObjectInspector;
import tod.core.database.browser.IObjectInspector.FieldEntryInfo;
import tod.core.database.browser.IObjectInspector.IEntryInfo;
import tod.core.database.structure.IFieldInfo;
import edu.ubc.mirrors.BoxingInstanceMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.ObjectMirror;

public class TODInstanceMirror {// extends BoxingInstanceMirror implements ObjectMirror {

//    private final IObjectInspector inspector;
    
//    @Override
//    public ClassMirror getClassMirror() {
//        // TODO Auto-generated method stub
//        return null;
//    }

//    @Override
//    public Object getBoxedValue(FieldMirror field) throws IllegalAccessException {
//        IFieldInfo fieldInfo = ((TODFieldMirror)field).field;
//        for (IEntryInfo entry : inspector.getEntries(0, Integer.MAX_VALUE)) {
//            FieldEntryInfo fieldEntry = (FieldEntryInfo)entry;
//            if (fieldEntry.getField().equals(fieldInfo)) {
//                return inspector.getEntryValue(entry);
//            }
//        }
//    }
//
//    @Override
//    public void setBoxedValue(FieldMirror field, Object o) throws IllegalAccessException {
//        throw new UnsupportedOperationException();
//    }

}
