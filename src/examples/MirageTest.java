package examples;

import edu.ubc.mirrors.ObjectMirage;

public class MirageTest {

    public static void main(String[] args) {
        Class<?> fooMirage = ObjectMirage.getMirageClass(Bar.class);
    }
    
}
