/*
 * DexPatcher - Copyright 2015-2020 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.codec.encoder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;

public class TypeInfoMapper {

	public static class TypeInfo {
		// Note: clearInterfaces can only contain elements if clearType is null.
		public String clearType;
		public Set<String> clearInterfaces = Collections.emptySet();
	}

	public static final TypeInfo NON_OBFUSCATED_TYPE_INFO = new TypeInfo();

	private static final TypeInfo IGNORED_TYPE_INFO = new TypeInfo();
	private static final String JAVA_LANG_OBJECT = "Ljava/lang/Object;";

	protected final TypeClassifier typeClassifier;
	protected final boolean processInterfaces;

	public TypeInfoMapper(TypeClassifier typeClassifier, boolean processInterfaces) {
		this.typeClassifier = typeClassifier;
		this.processInterfaces = processInterfaces;
	}

	public Map<String, TypeInfo> buildTypeInfoMap(DexFile dex) {
		Set<? extends ClassDef> classes = dex.getClasses();
		Map<String, ClassDef> classMap = new HashMap<>(classes.size());
		for (ClassDef classDef : classes) {
			classMap.put(classDef.getType(), classDef);
		}
		Map<String, TypeInfo> typeInfoMap = new HashMap<>(classes.size());
		for (ClassDef classDef : classes) {
			buildTypeInfo(typeInfoMap, classMap, classDef, classDef.getType());
		}
		return typeInfoMap;
	}

	private TypeInfo buildTypeInfo(Map<String, TypeInfo> typeInfoMap, Map<String, ClassDef> classMap,
			String type) {
		ClassDef classDef = classMap.get(type);
		if (classDef == null) return null;
		return buildTypeInfo(typeInfoMap, classMap, classDef, type);
	}

	private TypeInfo buildTypeInfo(Map<String, TypeInfo> typeInfoMap, Map<String, ClassDef> classMap,
			ClassDef classDef, String type) {
		TypeInfo info = typeInfoMap.get(type);
		if (info == NON_OBFUSCATED_TYPE_INFO) return null;
		if (info != null) return info;
		if (!typeClassifier.isObfuscatedType(type, true)) {
			typeInfoMap.put(type, NON_OBFUSCATED_TYPE_INFO);
			return null;
		}
		if (isIgnoredType(type)) {
			typeInfoMap.put(type, IGNORED_TYPE_INFO);
			return null;
		}
		info = new TypeInfo();
		// Early put to avoid infinite recursion while processing malformed dex files.
		typeInfoMap.put(type, info);
		String superclassType = classDef.getSuperclass();
		if (JAVA_LANG_OBJECT.equals(superclassType)) superclassType = null;
		Set<String> superclassClearInterfaces = null;
		if (superclassType != null && !isIgnoredType(superclassType)) {
			TypeInfo superclassInfo = buildTypeInfo(typeInfoMap, classMap, superclassType);
			if (superclassInfo == null) {
				// Type is external to dex file or not obfuscated.
				info.clearType = superclassType;
				return info;
			}
			if (superclassInfo.clearType != null) {
				info.clearType = superclassInfo.clearType;
				return info;
			}
			superclassClearInterfaces = superclassInfo.clearInterfaces;
		}
		if (!processInterfaces) {
			return info;
		}
		Set<String> clearInterfaces = new TreeSet<>();
		if (superclassClearInterfaces != null) {
			clearInterfaces.addAll(superclassClearInterfaces);
		}
		for (String interfaceType : classDef.getInterfaces()) {
			if (!isIgnoredType(interfaceType)) {
				TypeInfo interfaceInfo = buildTypeInfo(typeInfoMap, classMap, interfaceType);
				if (interfaceInfo == null) {
					// Type is external to dex file or not obfuscated.
					clearInterfaces.add(interfaceType);
				} else {
					if (interfaceInfo.clearType != null) {
						clearInterfaces.add(interfaceInfo.clearType);
					} else {
						clearInterfaces.addAll(interfaceInfo.clearInterfaces);
					}
				}
			}
		}
		if (!clearInterfaces.isEmpty()) info.clearInterfaces = clearInterfaces;
		return info;
	}

	protected boolean isIgnoredType(String type) {
		return false;
	}

}
