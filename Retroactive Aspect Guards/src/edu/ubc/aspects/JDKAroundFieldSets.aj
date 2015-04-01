package edu.ubc.aspects;

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
    
    void around(String s, int h): set(* String.hash) && cflow(within(Trace)) && this(s) && args(h) {
        // Don't proceed, just let it be recalculated every time
//        s.otherhash = h;
    }
}