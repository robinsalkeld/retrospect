/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
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
                String line = " & \\code{" + pattern.className + "} & \\code{" + pattern.methodName + "}";
                out.append(line.replace("$", "\\$"));
            }
            out.append("\\\\ \\hline\n");
        }
        out.append("\\end{tabular}");
        out.close();
    }
    
}
