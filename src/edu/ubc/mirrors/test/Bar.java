package edu.ubc.mirrors.test;

import java.util.HashMap;
import java.util.Map;

public class Bar {

    private Map<String, String> map = new HashMap<String, String>();
    
    public static void main(String[] args) throws InterruptedException {
        Thread deadThread = new Thread();
        
        Thread[] threads = new Thread[Thread.activeCount() + 5];
        int numThreads = Thread.enumerate(threads);
        for (int i = 0; i < numThreads; i++) {
            System.out.println(threads[i] + ": " + threads[i].isAlive());
        }
        
        
        Bar bar = new Bar(47);
        bar.map.put(new String("foo"), new String("bar"));
        System.out.println("ZZZZZZ");
        Thread.sleep(1000000);
    }
    
    int f;
    
    static int staticF = 5;
    
    public Bar(int f) {
        this.f = f;
    }
    
    public void bar(int x) {
        // TODO: deal with natives so we can reference the System class
//        System.out.println("Setting f to " + x);
        f = x;
//        System.out.println("f is now...");
//        System.out.println(f);
        int g = f + staticF;
    }
    
    public String toString() {
        f++;
        return "" + f + " <> "+ staticF + " map: " + map;
    }
    
    public void foo(boolean[] s) {
        
    }
    
    public void foo(byte[] s) {
        
    }
}
