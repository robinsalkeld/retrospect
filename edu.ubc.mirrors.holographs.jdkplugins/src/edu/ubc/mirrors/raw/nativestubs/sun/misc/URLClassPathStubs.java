package edu.ubc.mirrors.raw.nativestubs.sun.misc;

import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class URLClassPathStubs extends NativeStubs {

	public URLClassPathStubs(ClassHolograph klass) {
		super(klass);
	}

	@StubMethod
	public ObjectArrayMirror getLookupCacheURLs(ClassMirrorLoader loader) {
		// This signals that this JVM doesn't support the lookup cache
		return null;
	}
	
}
