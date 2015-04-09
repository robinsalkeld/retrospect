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
    
    void around(Class c, String n): set(* Class.name) && cflow(within(*.Racer)) && this(c) && args(n) {
        // Don't proceed, just let it be recalculated every time
    }
}
