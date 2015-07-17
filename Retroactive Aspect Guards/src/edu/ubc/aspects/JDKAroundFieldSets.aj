package edu.ubc.aspects;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

privileged aspect JDKAroundFieldSets {

    void around(): set(* java.lang.Class.name) {
        // Don't proceed(), just let it be recalculated every time
    }
//    
//    private final Map<String, Object> floatingDecimalStatics = new HashMap<String, Object>();
//    
//    void around(Object value): set(static * sun.misc.FloatingDecimal.*) && args(value) {
//        String fieldName = thisJoinPointStaticPart.getSignature().getName();
//        floatingDecimalStatics.put(fieldName, value);
//    }
//    
//    Object around(): get(static * sun.misc.FloatingDecimal.*) {
//        String fieldName = thisJoinPointStaticPart.getSignature().getName();
//        return floatingDecimalStatics.get(fieldName);
//    }
    
    // Standard streams
    
    private static final OutputStream stdoutWormhole = new WormholeStream(1);
    private static final PrintStream newStdout = new PrintStream(stdoutWormhole);
    
    PrintStream around(): get(* System.out) {
        return newStdout;
    }
    
    private static final OutputStream stderrWormhole = new WormholeStream(2);
    private static final PrintStream newStderr = new PrintStream(stderrWormhole);
    
    PrintStream around(): get(* System.err) {
        return newStderr;
    }
}
