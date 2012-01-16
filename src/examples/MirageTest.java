package examples;


public class MirageTest {

    Class<?> c;
    
    public MirageTest(Class<?> c) {
        this.c = c;
    }
    
    public static void main(String[] args) throws Exception {
        new MirageTest(Bar.class);
//        new FieldMapMirror<Bar>(Bar.class);
//        System.out.println(Bar.class.getClassLoader());
//        Bar barMirage = ObjectMirage.make(fooMirror);
//        barMirage.bar();
    }
    
}
