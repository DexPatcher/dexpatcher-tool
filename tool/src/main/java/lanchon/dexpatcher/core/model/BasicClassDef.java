/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core.model;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Iterables;

import org.jf.dexlib2.base.reference.BaseTypeReference;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.util.FieldUtil;
import org.jf.dexlib2.util.MethodUtil;

public class BasicClassDef extends BaseTypeReference implements ClassDef {

	private final String type;
	private final int accessFlags;
	private final String superclass;
	private final List<String> interfaces;
	private final String sourceFile;
	private final Set<? extends Annotation> annotations;
	private final Iterable<? extends Field> fields;
	private final Iterable<? extends Field> staticFields;
	private final Iterable<? extends Field> instanceFields;
	private final Iterable<? extends Method> methods;
	private final Iterable<? extends Method> directMethods;
	private final Iterable<? extends Method> virtualMethods;

	public BasicClassDef(
			String type,
			int accessFlags,
			String superclass,
			List<String> interfaces,
			String sourceFile,
			Set<? extends Annotation> annotations,
			Iterable<? extends Field> fields,
			Iterable<? extends Method> methods
	) {
		this.type = type;
		this.accessFlags = accessFlags;
		this.superclass = superclass;
		this.interfaces = interfaces;
		this.sourceFile = sourceFile;
		this.annotations = annotations;
		this.fields = fields;
		this.staticFields = Iterables.filter(fields, FieldUtil.FIELD_IS_STATIC);
		this.instanceFields = Iterables.filter(fields, FieldUtil.FIELD_IS_INSTANCE);
		this.methods = methods;
		this.directMethods = Iterables.filter(methods, MethodUtil.METHOD_IS_DIRECT);
		this.virtualMethods = Iterables.filter(methods, MethodUtil.METHOD_IS_VIRTUAL);
	}

	public BasicClassDef(
			String type,
			int accessFlags,
			String superclass,
			List<String> interfaces,
			String sourceFile,
			Set<? extends Annotation> annotations,
			Iterable<? extends Field> staticFields,
			Iterable<? extends Field> instanceFields,
			Iterable<? extends Method> directMethods,
			Iterable<? extends Method> virtualMethods
	) {
		this.type = type;
		this.accessFlags = accessFlags;
		this.superclass = superclass;
		this.interfaces = interfaces;
		this.sourceFile = sourceFile;
		this.annotations = annotations;
		this.fields = Iterables.concat(staticFields, instanceFields);
		this.staticFields = staticFields;
		this.instanceFields = instanceFields;
		this.methods = Iterables.concat(directMethods, virtualMethods);
		this.directMethods = directMethods;
		this.virtualMethods = virtualMethods;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public int getAccessFlags() {
		return accessFlags;
	}

	@Override
	public String getSuperclass() {
		return superclass;
	}

	@Override
	public List<String> getInterfaces() {
		return interfaces;
	}

	@Override
	public String getSourceFile() {
		return sourceFile;
	}

	@Override
	public Set<? extends Annotation> getAnnotations() {
		return annotations;
	}

	@Override
	public Iterable<? extends Field> getFields() {
		return fields;
	}

	@Override
	public Iterable<? extends Field> getStaticFields() {
		return staticFields;
	}

	@Override
	public Iterable<? extends Field> getInstanceFields() {
		return instanceFields;
	}

	@Override
	public Iterable<? extends Method> getMethods() {
		return methods;
	}

	@Override
	public Iterable<? extends Method> getDirectMethods() {
		return directMethods;
	}

	@Override
	public Iterable<? extends Method> getVirtualMethods() {
		return virtualMethods;
	}

}
