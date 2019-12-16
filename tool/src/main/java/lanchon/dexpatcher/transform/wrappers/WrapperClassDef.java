/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.wrappers;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Iterators;
import org.jf.dexlib2.base.reference.BaseTypeReference;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;

public class WrapperClassDef extends BaseTypeReference implements ClassDef {

	protected final ClassDef wrappedClassDef;

	public WrapperClassDef(ClassDef wrappedClassDef) {
		this.wrappedClassDef = wrappedClassDef;
	}

	@Override
	public String getType() {
		return wrappedClassDef.getType();
	}

	@Override
	public int getAccessFlags() {
		return wrappedClassDef.getAccessFlags();
	}

	@Override
	public String getSuperclass() {
		return wrappedClassDef.getSuperclass();
	}

	@Override
	public List<String> getInterfaces() {
		return wrappedClassDef.getInterfaces();
	}

	@Override
	public String getSourceFile() {
		return wrappedClassDef.getSourceFile();
	}

	@Override
	public Set<? extends Annotation> getAnnotations() {
		return wrappedClassDef.getAnnotations();
	}

	@Override
	public Iterable<? extends Field> getStaticFields() {
		return wrappedClassDef.getStaticFields();
	}

	@Override
	public Iterable<? extends Field> getInstanceFields() {
		return wrappedClassDef.getInstanceFields();
	}

	@Override
	public Iterable<? extends Method> getDirectMethods() {
		return wrappedClassDef.getDirectMethods();
	}

	@Override
	public Iterable<? extends Method> getVirtualMethods() {
		return wrappedClassDef.getVirtualMethods();
	}

	@Override
	public final Iterable<? extends Field> getFields() {
		return new Iterable<Field>() {
			@Override
			public Iterator<Field> iterator() {
				return Iterators.concat(getStaticFields().iterator(), getInstanceFields().iterator());
			}
		};
	}

	@Override
	public final Iterable<? extends Method> getMethods() {
		return new Iterable<Method>() {
			@Override
			public Iterator<Method> iterator() {
				return Iterators.concat(getDirectMethods().iterator(), getVirtualMethods().iterator());
			}
		};
	}

}