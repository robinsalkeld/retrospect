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

import java.util.HashMap;
import java.util.Map;

import tod.core.database.browser.IObjectInspector;
import tod.core.database.browser.IObjectInspector.FieldEntryInfo;
import tod.core.database.browser.IObjectInspector.IEntryInfo;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IFieldInfo;
import edu.ubc.mirrors.BoxingInstanceMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.ObjectMirror;

public class TODInstanceMirror extends BoxingInstanceMirror implements ObjectMirror {

    protected final TODVirtualMachineMirror vm;
    private final IObjectInspector inspector;
    private ILogEvent referenceEvent;
    private final Map<IFieldInfo, IEntryInfo> entries = new HashMap<IFieldInfo, IEntryInfo>();
    
    public TODInstanceMirror(TODVirtualMachineMirror vm, IObjectInspector inspector) {
        this.vm = vm;
        this.inspector = inspector;
        for (IEntryInfo entry : inspector.getEntries(0, Integer.MAX_VALUE)) {
            FieldEntryInfo fieldEntry = (FieldEntryInfo)entry;
            entries.put(fieldEntry.getField(), fieldEntry);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TODInstanceMirror)) {
            return false;
        }
        
        TODInstanceMirror other = (TODInstanceMirror)obj;
        return vm.equals(other.vm) &&
                inspector.equals(other.inspector);
    }
    
    @Override
    public int hashCode() {
        return vm.hashCode() * inspector.hashCode();
    }
    
    @Override
    public ClassMirror getClassMirror() {
        ClassMirror result = vm.makeClassMirror(inspector.getType());
        if (result == null) {
            throw new IllegalStateException();
        } 
        return result;
    }

    @Override
    public Object getBoxedValue(FieldMirror field) throws IllegalAccessException {
        ILogEvent currentLogEvent = vm.requestManager.currentLogEvent();
        if (!currentLogEvent.equals(referenceEvent)) {
            inspector.setReferenceEvent(currentLogEvent);
            referenceEvent = currentLogEvent;
        }
        
        IFieldInfo fieldInfo = ((TODFieldMirror)field).field;
        IEntryInfo entry = entries.get(fieldInfo);
        if (entry == null) {
            throw new IllegalStateException("Couldn't find field " + fieldInfo + " on object " + inspector);
        }
        
        Object result = vm.wrapEntryValues(field.getType(), inspector.getEntryValue(entry));
        
        return result;
    }

    @Override
    public void setBoxedValue(FieldMirror field, Object o) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int identityHashCode() {
        return (int)inspector.getObject().getId();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " on " + inspector.getObject() + " : " + inspector.getType();
    }

    @Override
    public boolean canLock() {
        return false;
    }
}
