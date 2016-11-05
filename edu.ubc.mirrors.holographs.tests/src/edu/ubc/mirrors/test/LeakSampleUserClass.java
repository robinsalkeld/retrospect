package edu.ubc.mirrors.test;


public class LeakSampleUserClass {
    static MyVector myVector = new MyVector(100);
    
    private static class MyVector {
        private int size = 0;
        private Object[] array;
        
        public MyVector(int capacity) {
            array = new Object[capacity];
        }
        public void add(Object o) {
            array[size++] = o;
        }
        public void removeElementAt(int i) {
            while (i < size - 1) {
                array[i] = array[i + 1];
                i++;
            }
            array[i] = null;
            size--;
        }
    }
    
    private static class MyObject {
        private int x;
        public MyObject(int x) {
            this.x = x;
        }
    }
    
    public static void slowlyLeakingVector(int iter, int count) { 
        for (int i=0; i<iter; i++) {
            System.out.println("Iteration: " + i);
            for (int n=0; n<count; n++) {
//                System.out.println("Adding " + n);
                myVector.add(new MyObject(n+i));
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
