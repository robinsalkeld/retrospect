package edu.ubc.mirrors.test;

import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParser;

public class PointcutParsing {

    public static void main(String args[]) {
        PointcutParser pp = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution();
        PointcutExpression pe = pp.parsePointcutExpression("cflow(execution(* *(..)))");
    }
    
}
