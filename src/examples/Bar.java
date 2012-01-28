package examples;

public class Bar {

    int f;
    
    public Bar(int f) {
        this.f = f;
    }
    
    public void bar(int x) {
        // TODO: deal with natives so we can reference the System class
//        System.out.println("Setting f to " + x);
        f = x;
//        System.out.println("f is now...");
//        System.out.println(f);
        int g = f;
    }
}
