package edu.ubc.mirrors.eclipse.mat;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FrameMirror;

public class HeapDumpFrameMirror implements FrameMirror {

    private final HeapDumpVirtualMachineMirror vm;
    
    private static final Pattern FRAME_PATTERN = Pattern.compile("at (.*)\\.(.*)\\(.*\\).* \\((?:(.*):(\\d*)|(.*))\\)");
    
    private final ClassMirror declaringClass;
    private final String methodName;
    private final String fileName;
    private final int lineNumber;
    
    public HeapDumpFrameMirror(HeapDumpVirtualMachineMirror vm, String text) {
	this.vm = vm;
	Matcher m = FRAME_PATTERN.matcher(text);
        if (m.matches()) {
            String className = m.group(1);
            
            // Unfortunately here we only have class names rather than actual classes
            // so if any are ambiguous we're hooped.
            List<ClassMirror> matchingClasses = vm.findAllClasses(className, false);
            if (matchingClasses.isEmpty()) {
                // This indicates an error in the underlying VM
                throw new InternalError();
            } else if (matchingClasses.size() > 1) {
                // This is just unfortunate but could happen
                throw new InternalError("Ambiguous class name on stack: " + className);
            }
            declaringClass = matchingClasses.get(0);
            methodName = m.group(2);
            fileName = m.group(3);
            String lineTxt = m.group(4);
            lineNumber = (lineTxt != null ? Integer.parseInt(lineTxt) : -1);
        } else {
            throw new RuntimeException("Unexpected frame text format: " + text);
        }

    }
    
    @Override
    public ClassMirror declaringClass() {
	return declaringClass;
    }

    @Override
    public String methodName() {
	return methodName;
    }

    @Override
    public String fileName() {
	return fileName;
    }

    @Override
    public int lineNumber() {
	return lineNumber;
    }

}
