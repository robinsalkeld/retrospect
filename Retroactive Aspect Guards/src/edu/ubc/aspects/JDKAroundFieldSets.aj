package edu.ubc.aspects;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

privileged aspect JDKAroundFieldSets {

    void around(): set(* java.lang.Class.name) {
        // Don't proceed(), just let it be recalculated every time
    }
    
//    void around(): set(* java.lang.String.hash) {
//        // Don't proceed(), just let it be recalculated every time
//    }
    
    private final Map<String, Object> floatingDecimalStatics = new HashMap<String, Object>();
    
    void around(Object value): set(static * sun.misc.FloatingDecimal.*) && args(value) {
        String fieldName = thisJoinPointStaticPart.getSignature().getName();
        floatingDecimalStatics.put(fieldName, value);
    }
    
    Object around(): get(static * sun.misc.FloatingDecimal.*) {
        String fieldName = thisJoinPointStaticPart.getSignature().getName();
        return floatingDecimalStatics.get(fieldName);
    }
    
    // Standard streams
    
    private static final ByteArrayOutputStream newStdoutBaos = new ByteArrayOutputStream();
    private static final PrintStream newStdout = new PrintStream(newStdoutBaos);
    
    PrintStream around(): get(* System.out) {
        return newStdout;
    }
    
    private static final ByteArrayOutputStream newStderrBaos = new ByteArrayOutputStream();
    private static final PrintStream newStderr = new PrintStream(newStderrBaos);
    
    PrintStream around(): get(* System.err) {
        return newStderr;
    }
    
}
