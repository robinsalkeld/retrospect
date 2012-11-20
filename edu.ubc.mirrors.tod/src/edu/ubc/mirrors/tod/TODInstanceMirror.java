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
