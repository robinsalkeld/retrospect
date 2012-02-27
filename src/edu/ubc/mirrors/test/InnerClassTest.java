package edu.ubc.mirrors.test;

public class InnerClassTest {

    private final int secret = 12;
    
    private static class StaticInner {
        
        protected final int foo;
        
        public StaticInner(int foo) {
            this.foo = foo;
        }
    }
    
    private static class StaticInnerSub extends StaticInner {
        int bar;  
        public StaticInnerSub(int foo) {
            super(foo);
        }
        
    }
    
    public void doStuff(int x) {
        final int boo = 2 + x;
        final int blar = 4 + x;
        StaticInner myInner = new StaticInner(secret + boo) {
            @Override
            public int hashCode() {
                return boo + blar;
            }
        };
    }
    
}
