/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
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
        Thread deadThread = new Thread();
        
        Thread[] threads = new Thread[Thread.activeCount() + 5];
        int numThreads = Thread.enumerate(threads);
        for (int i = 0; i < numThreads; i++) {
            System.out.println(threads[i] + ": " + threads[i].isAlive());
        }
        
        
        Bar bar = new Bar(47);
        System.out.println(bar.hashCode());
        bar.map.put(new String("foo"), new String("bar"));

        while (true) {
            bar.bar(12);
        }
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
