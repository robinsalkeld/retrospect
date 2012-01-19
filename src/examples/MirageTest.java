package examples;


public class MirageTest {

    String c;
    
    public MirageTest(String c) {
        this.c = c;
    }
    
    public static void main(String[] args) throws Exception {
        new MirageTest("Bar");
//        new FieldMapMirror<Bar>(Bar.class);
//        System.out.println(Bar.class.getClassLoader());
//        Bar barMirage = ObjectMirage.make(fooMirror);
//        barMirage.bar();
    }
    
}
