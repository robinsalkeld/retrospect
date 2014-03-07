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
import tod.core.database.browser.IObjectInspector.ArraySlotEntryInfo;
import tod.core.database.browser.IObjectInspector.FieldEntryInfo;
import tod.core.database.browser.IObjectInspector.IEntryInfo;
import tod.core.database.structure.IFieldInfo;
import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.BoxingArrayMirror;
import edu.ubc.mirrors.BoxingInstanceMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

public class TODArrayMirror extends BoxingArrayMirror implements ObjectArrayMirror {

    private final TODVirtualMachineMirror vm;
    private final IObjectInspector inspector;
    
    public TODArrayMirror(TODVirtualMachineMirror vm, IObjectInspector inspector) {
        this.vm = vm;
        this.inspector = inspector;
    }

    @Override
    public ClassMirror getClassMirror() {
        return vm.makeClassMirror(inspector.getType());
    }

    @Override
    public int length() {
        return inspector.getEntryCount();
    }
    
    @Override
    public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
        return (ObjectMirror)getBoxedValue(index);
    }
    
    @Override
    protected Object getBoxedValue(int index) {
        // TODO-RS: Only do this when the VM actually changes events!
        inspector.setReferenceEvent(vm.requestManager.currentLogEvent());
        
        // TODO-RS: Make this faster by caching the IEntryInfo.
        // Will be tricky since the IEntryInfos seem to be per-instance instead of
        // per-class.
        IEntryInfo entry = inspector.getEntries(0, Integer.MAX_VALUE).get(index);
        ArraySlotEntryInfo arraySlotEntry = (ArraySlotEntryInfo)entry;
        return vm.wrapEntryValues(getClassMirror().getComponentClassMirror(), inspector.getEntryValue(arraySlotEntry));
    }
    
    @Override
    public void set(int index, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        setBoxedValue(index, o);
        
    }
    
    @Override
    protected void setBoxedValue(int index, Object o) throws ArrayIndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int identityHashCode() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
    public String toString() {
        return getClass().getSimpleName() + " on " + inspector.getObject() + " : " + inspector.getType();
    };
}
