/*
 * DexPatcher - Copyright 2015-2020 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.mapper.map.builder;

import lanchon.dexpatcher.core.util.DexUtils;
import lanchon.dexpatcher.core.util.Label;
import lanchon.dexpatcher.transform.mapper.map.DexMap;

import com.google.common.collect.ImmutableList;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;

public class CompositeMapBuilder implements MapBuilder {

	public static final String ESCAPE_PREFIX = "/";

	protected static final int ESCAPE_PREFIX_LENGTH = ESCAPE_PREFIX.length();

	public static MapBuilder of(MapBuilder wrappedMapBuilder, DexMap inverseComposeMap) {
		if (inverseComposeMap != null) {
			return new CompositeMapBuilder(wrappedMapBuilder, inverseComposeMap);
		} else {
			return wrappedMapBuilder;
		}
	}

	protected final MapBuilder wrappedMapBuilder;
	protected final DexMap inverseComposeMap;

	public CompositeMapBuilder(MapBuilder wrappedMapBuilder, DexMap inverseComposeMap) {
		this.wrappedMapBuilder = wrappedMapBuilder;
		this.inverseComposeMap = inverseComposeMap;
	}

	@Override
	public MemberMapBuilder addClassMapping(String name, String newName) {
		final String definingClass = forwardResolveElementalType(name);
		name = inverseResolveElementalType(null, name);
		if (DexUtils.isClassDescriptor(newName) && newName.startsWith(ESCAPE_PREFIX, 1)) throw mappingOnRHS();
		final MemberMapBuilder wrappedMemberMapBuilder = wrappedMapBuilder.addClassMapping(name, newName);
		return new MemberMapBuilder() {
			@Override
			public void addFieldMapping(String type, String name, String newName) {
				if (name.startsWith(ESCAPE_PREFIX)) {
					name = name.substring(ESCAPE_PREFIX_LENGTH);
					String forwardResolvedType = forwardResolveType(type);
					FieldReference field = new ImmutableFieldReference(definingClass, name, forwardResolvedType);
					name = inverseComposeMap.getFieldMapping(field);
					if (name == null) throw mappingNotFound(field);
				}
				if (newName.startsWith(ESCAPE_PREFIX)) throw mappingOnRHS();
				type = inverseResolveType(definingClass, type);
				wrappedMemberMapBuilder.addFieldMapping(type, name, newName);
			}
			@Override
			public void addMethodMapping(String[] parameterTypes, String returnType, String name, String newName) {
				if (name.startsWith(ESCAPE_PREFIX)) {
					name = name.substring(ESCAPE_PREFIX_LENGTH);
					int length = parameterTypes.length;
					String[] forwardResolvedParams = new String[length];
					for (int i = 0; i < length; i++) {
						forwardResolvedParams[i] = forwardResolveType(parameterTypes[i]);
					}
					String forwardResolvedReturnType = forwardResolveType(returnType);
					MethodReference method = new ImmutableMethodReference(definingClass, name,
							ImmutableList.copyOf(forwardResolvedParams), forwardResolvedReturnType);
					name = inverseComposeMap.getMethodMapping(method);
					if (name == null) throw mappingNotFound(method);
				}
				if (newName.startsWith(ESCAPE_PREFIX)) throw mappingOnRHS();
				int length = parameterTypes.length;
				String[] inverseResolvedParams = new String[length];
				for (int i = 0; i < length; i++) {
					inverseResolvedParams[i] = inverseResolveType(definingClass, parameterTypes[i]);
				}
				returnType = inverseResolveType(definingClass, returnType);
				wrappedMemberMapBuilder.addMethodMapping(inverseResolvedParams, returnType, name, newName);
			}
		};
	}

	protected String inverseResolveType(String definingClass, String type) {
		int length = type.length();
		if (length == 0 || type.charAt(0) != '[') return inverseResolveElementalType(definingClass, type);
		int start = 1;
		while (start < length && type.charAt(start) == '[') start++;
		String elementalType = type.substring(start);
		String resolvedElementalType = inverseResolveElementalType(definingClass, elementalType);
		//if (resolvedElementalType.equals(elementalType)) return type;
		StringBuilder sb = new StringBuilder(start + resolvedElementalType.length());
		sb.append(type, 0, start).append(resolvedElementalType);
		return sb.toString();
	}

	protected String inverseResolveElementalType(String definingClass, String type) {
		if (DexUtils.isClassDescriptor(type) && type.startsWith(ESCAPE_PREFIX, 1)) {
			int length = type.length();
			type = new StringBuilder(length - ESCAPE_PREFIX_LENGTH).append('L')
					.append(type, 1 + ESCAPE_PREFIX_LENGTH, length).toString();
			String resolvedType = inverseComposeMap.getClassMapping(type);
			if (resolvedType == null) throw mappingNotFound(definingClass, type);
			type = resolvedType;
		}
		return type;
	}

	protected static String forwardResolveType(String type) {
		int length = type.length();
		if (length == 0 || type.charAt(0) != '[') return forwardResolveElementalType(type);
		int start = 1;
		while (start < length && type.charAt(start) == '[') start++;
		String elementalType = type.substring(start);
		String resolvedElementalType = forwardResolveElementalType(elementalType);
		//if (resolvedElementalType.equals(elementalType)) return type;
		StringBuilder sb = new StringBuilder(start + resolvedElementalType.length());
		sb.append(type, 0, start).append(resolvedElementalType);
		return sb.toString();
	}

	protected static String forwardResolveElementalType(String type) {
		if (DexUtils.isClassDescriptor(type) && type.startsWith(ESCAPE_PREFIX, 1)) {
			int length = type.length();
			type = new StringBuilder(length - ESCAPE_PREFIX_LENGTH).append('L')
					.append(type, 1 + ESCAPE_PREFIX_LENGTH, length).toString();
		}
		return type;
	}

	protected static BuilderException mappingNotFound(String definingClass, String type) {
		return new BuilderException(
				(definingClass != null ? "type '" + Label.fromClassDescriptor(definingClass) + "': " : "") +
				"missing composed mapping for type '" + Label.fromClassDescriptor(type) + "'");
	}

	protected static BuilderException mappingNotFound(FieldReference field) {
		return new BuilderException(
				"type '" + Label.fromClassDescriptor(field.getDefiningClass()) + "': " +
				"missing composed mapping for field '" + Label.ofField(field) + "'");
	}

	protected static BuilderException mappingNotFound(MethodReference method) {
		return new BuilderException(
				"type '" + Label.fromClassDescriptor(method.getDefiningClass()) + "': " +
				"missing composed mapping for method '" + Label.ofMethod(method) + "'");
	}

	protected static BuilderException mappingOnRHS() {
		return new BuilderException(
				"composed mappings are illegal on the right-hand side of mapping expressions");
	}

}
