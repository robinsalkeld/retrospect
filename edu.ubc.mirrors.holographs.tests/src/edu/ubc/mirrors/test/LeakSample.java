package edu.ubc.mirrors.test;

import java.util.Vector;

public class LeakSample {
    static Vector myVector = new Vector();
    
    public static void slowlyLeakingVector(int iter, int count) { 
        for (int i=0; i<iter; i++) {
            System.out.println("Iteration: " + i);
            for (int n=0; n<count; n++) {
//                System.out.println("Adding " + n);
                myVector.add(Integer.toString(n+i));
            }
            for (int n=count-1; n>0; n--) { 
//                System.out.println("Removing at " + n);
                // Oops, it should be n>=0 
                myVector.removeElementAt(n);
            }
        }
    }
    
    public static void main(String[] args) {
        slowlyLeakingVector(1, 100);
    }
}
