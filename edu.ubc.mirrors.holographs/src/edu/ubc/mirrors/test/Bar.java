package edu.ubc.mirrors.test;

import java.util.HashMap;
import java.util.Map;

public class Bar {

    static {
        System.out.println("Wassaaaaaaaap???");
    }
    
    private Map<String, String> map = new HashMap<String, String>();
    
    private static final Class<?> VOID_TYPE = Void.TYPE;
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println(Long.toBinaryString(5514902520l));
        System.out.println(Integer.toBinaryString(1607546255));
        
        Thread deadThread = new Thread();
        
        Thread[] threads = new Thread[Thread.activeCount() + 5];
        int numThreads = Thread.enumerate(threads);
        for (int i = 0; i < numThreads; i++) {
            System.out.println(threads[i] + ": " + threads[i].isAlive());
        }
        
        
        Bar bar = new Bar(47);
        System.out.println(bar.hashCode());
        bar.map.put(new String("foo"), new String("bar"));
        Thread.sleep(10000);
        System.out.println("ZZZZZZ");
        Thread.sleep(1000000000);
    }
    
    int f;
    
    static int staticF = 5;
    
    public Bar() {
        this.f = 5;
    }
    
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
