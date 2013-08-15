package edu.ubc.mirrors.holographs.jdkplugins;

import java.util.regex.Pattern;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.holographs.ClassMirrorBytecodeProvider;

public class GeneratedBytecodeProvider implements ClassMirrorBytecodeProvider {

    private static final Pattern PROXY_CLASS_NAME_PATTERN = Pattern.compile("$Proxy(\\d+)");
    
    @Override
    public byte[] getBytecode(ClassMirror classMirror) {
        if (PROXY_CLASS_NAME_PATTERN.matcher(classMirror.getClassName()).matches()) {
            return null;
        }

        return null;
    }

}
