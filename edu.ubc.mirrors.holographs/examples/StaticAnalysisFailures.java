package examples;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Examples of edge cases for the type system; which assignments to Foo#bar are legal?
 * 
 * @author robinsalkeld
 */
public class StaticAnalysisFailures {

    public static @interface Old {}
    public static @interface New {}
    
    // @Old annotation implies class was loaded in original execution,
    // and hence the static "zero" field also has the @Old annotation.
    public static @Old class Foo {
        
        static final Foo zero = new Foo(0);
        
        public int bar;
        
        Foo(int bar) {
            this.bar = bar;
        }
    }
    
    public static interface A {
        public Foo getFoo();
    }
    public static class B implements A {
        @Override
        public Foo getFoo() {
            return new Foo(7);
        }
    }
    public static class C implements A {
        @Override
        public Foo getFoo() {
            return Foo.zero;
        }
    }
    public static class D implements A {
        @Override
        public Foo getFoo() {
            if (new Random().nextBoolean()) {
                return Foo.zero;
            } else {
                return new Foo(5);
            }
            
        }
    }
    
    public static void main(String[] args) {
        // There are a few options for how to handle this:
        //
        //  1. Assign one space to the result of A#getFoo, so the presence of
        //     both B and C in the system is an error.
        //
        //  2. Assign a different space to the return value for each A variable.
        //     So here a would have a type something like "@New @Returns(m = "getFoo", a = @Old)"
        //     I'm worried about the explosion of complexity of that approach.
        //
        //  3. Require the dynamic type of each variable to be computable at compile time.
        //     This would make a lot more code illegal, and I suspect despite what you were
        //     saying about the likely true prevalence of polymorphism that it won't be feasible.
        //
        //  4. ???
        //
        A a = new C();
        a.getFoo().bar = 2;
        
        // a3.getFoo() clearly isn't typeable as either @Old or @New.
        // Only a dynamic approach could allow this one
        A a3 = new D();
        a3.getFoo().bar = 2;
        
        // Also illegal in type system for the same reason as #2
        A a4;
        a4 = new B();
        a4.getFoo().bar = 2;
        a4 = new C();
        a4.getFoo().bar = 2;
        
        // OK, if we assume JSR 308 (attaching annotations to all occurrences of types),
        // since the types would be List<@Old Foo> and List<@New Foo>.
        // Really the problem with the first example (or toString()) is that there
        // isn't already a type variable in the right place.
        List<Foo> list1 = Collections.singletonList(Foo.zero);
        list1.get(0).bar = 2;
        List<Foo> list2 = Collections.singletonList(new Foo(42));
        list2.get(0).bar = 2;
    }
}
