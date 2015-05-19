package edu.ubc.aspects;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

privileged aspect JDKAroundFieldSets {

//    private int String.otherhash = 0;
//    
//    int around(String s): execution(* String.hash()) && this(s) {
//        if (s.otherhash != 0) {
//            return s.otherhash;
//        } else {
//            return proceed(s);
//        }
//    }
//    
    void around(): set(* java.lang.Class.name) {
        // Don't proceed(), just let it be recalculated every time
    }
    
    void around(): set(* java.lang.String.hash) {
        // Don't proceed(), just let it be recalculated every time
    }
    
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
    
//    private static final ByteArrayOutputStream newStdoutBaos = new ByteArrayOutputStream();
//    private static final PrintStream newStdout = new PrintStream(newStdoutBaos);
//    
//    PrintStream around(): get(PrintStream System.out) {
//        return newStdout;
//    }
//    
//    private static final ByteArrayOutputStream newStderrBaos = new ByteArrayOutputStream();
//    private static final PrintStream newStderr = new PrintStream(newStderrBaos);
//    
//    PrintStream around(): get(PrintStream System.err) {
//        return newStderr;
//    }
    
}
