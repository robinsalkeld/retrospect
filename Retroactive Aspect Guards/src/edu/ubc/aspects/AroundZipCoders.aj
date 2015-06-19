package edu.ubc.aspects;

import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;


public aspect AroundZipCoders {

    private Map<Object, CharsetEncoder> moreEncs = new HashMap<Object, CharsetEncoder>();
    
    void around(Object coder, CharsetEncoder enc) : 
        set(* java.util.zip.ZipCoder.enc) && this(coder) && args(enc) {
        
        moreEncs.put(coder, enc);
    }
    
    CharsetEncoder around(Object coder) : 
        get(* java.util.zip.ZipCoder.enc) && this(coder) {
        
        return moreEncs.get(coder);
    }
}
