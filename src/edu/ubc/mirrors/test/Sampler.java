package edu.ubc.mirrors.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class Sampler {

    public static void main(String[] args) throws FileNotFoundException {
        String path = args[0];
        int size = Integer.parseInt(args[1]);
        int sampleSize = Integer.parseInt(args[2]);
        Random random = new Random();
        Set<Integer> ids = new TreeSet<Integer>();
        while (sampleSize-- > 0) {
            int id;
            do {
                id = random.nextInt(size);
            } while (!ids.add(id));
        }
        PrintStream ps = new PrintStream(new FileOutputStream(new File(path)));
        for (int id : ids) {
            ps.println(id);
        }
        ps.close();
    }
    
}
