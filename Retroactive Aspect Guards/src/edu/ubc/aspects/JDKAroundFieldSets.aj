package edu.ubc.aspects;

import java.util.HashMap;
import java.util.Map;

public aspect JDKAroundFieldSets {

//    private int String.otherhash = 0;
//    
//    int around(String s): execution(* String.hash()) && this(s) {
//        if (s.otherhash != 0) {
//            return s.otherhash;
//        } else {
//            return proceed(s);
//        }
//    }
    
    void around(): set(* java.lang.Class.name) {
        // Don't proceed(), just let it be recalculated every time
    }
    
    private static Map<String, Object> floatingDecimalStatics = new HashMap<String, Object>();
    
    void around(Object value): set(static * sun.misc.FloatingDecimal.perThreadBuffer) && args(value) {
        String fieldName = thisJoinPointStaticPart.getSignature().getName();
        floatingDecimalStatics.put(fieldName, value);
    }
    
    Object around(): get(static * sun.misc.FloatingDecimal.perThreadBuffer) {
        String fieldName = thisJoinPointStaticPart.getSignature().getName();
        return floatingDecimalStatics.get(fieldName);
    }
//    
//    void around(): set(* String.hash) {
//        // Don't proceed(), just let it be recalculated every time
//    }
}
