package edu.ubc.mirrors.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.ClassHolograph.MethodPattern;

public class ForbiddenMethodsTableDumper {

    public static void main(String[] args) throws IOException {
        String outputPath = args[0];
        
        Writer out = new OutputStreamWriter(new FileOutputStream(outputPath));

        out.append("\\begin{tabular}{ l | l | l }\n");
        out.append("Category & Class & Method \\\\ \\hline \\hline\n");
        Map<String, List<MethodPattern>> patternsByCategory = new TreeMap<String, List<MethodPattern>>();
        for (MethodPattern pattern : ClassHolograph.illegalMethodPatterns) {
            List<MethodPattern> patterns = patternsByCategory.get(pattern.category);
            if (patterns == null) {
                patterns = new ArrayList<MethodPattern>();
                patternsByCategory.put(pattern.category, patterns);
            }
            patterns.add(pattern);
        }
        for (Map.Entry<String, List<MethodPattern>> entry : patternsByCategory.entrySet()) {
            String category = entry.getKey();
            List<MethodPattern> patterns = entry.getValue();
            Collections.sort(patterns);
            out.append("\\multirow{" + patterns.size() + "}{*}{" + category + "}");
            boolean first = true;
            for (MethodPattern pattern : patterns) {
                if (first) {
                    first = false;
                } else {
                    out.append("\\\\ \\cline{2-3}\n");
                }
                String line = " & " + pattern.className + " & " + pattern.methodName;
                out.append(line.replace("$", "\\$"));
            }
            out.append("\\\\ \\hline\n");
        }
        out.append("\\end{tabular}");
        out.close();
    }
    
}
