package edu.ubc.mirrors.test;

import abc.aspectj.ast.Pointcut;
import edu.ubc.retrospect.PointcutParser;

public class PointcutParsing {

    public static void main(String args[]) throws Exception {
        String expr = "execution(* *(..)) && foobar()";
        Pointcut pc = new PointcutParser().parse(expr);
        System.out.println(pc.pcRefs().toArray());
    }
    
}