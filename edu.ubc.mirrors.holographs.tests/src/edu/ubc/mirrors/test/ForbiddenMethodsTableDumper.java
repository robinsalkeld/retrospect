package edu.ubc.mirrors.test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import edu.ubc.mirrors.holographs.ClassHolograph;

public class ForbiddenMethodsTableDumper {

    public static void main(String[] args) throws IOException {
        String outputPath = args[0];
        
        
        
        Writer out = new OutputStreamWriter(new FileOutputStream(outputPath));

        out.append("\\begin{tabular}{ l | l | l }\n");
        out.append("Category & Class & Method \\\\ \\hline \\hline\n");
        for (ClassHolograph.MethodPattern pattern : ClassHolograph.illegalMethodPatterns) {
            String line = pattern.category + " & " + pattern.methodName + " & " + pattern.className + "\\\\ \\hline\n";
            out.append(line.replace("$", "\\$"));
        }
        out.append("\\end{tabular}");
        out.close();
    }
    
}
