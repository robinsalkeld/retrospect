package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class Class {

	public static class AtomicStubs extends NativeStubs {

		public AtomicStubs(ClassHolograph klass) {
			super(klass);
		}
		
		@StubMethod
		public boolean casReflectionData(ClassMirror clazz, InstanceMirror oldData, InstanceMirror newData) throws IllegalAccessException {
			ClassMirror classClass = getVM().findBootstrapClassMirror(java.lang.Class.class.getName());
			FieldMirror reflectionDataField = classClass.getDeclaredField("reflectionData");
			return Reflection.compareAndSwapObject(clazz, reflectionDataField, 
					oldData, newData);
		}
	}
}
