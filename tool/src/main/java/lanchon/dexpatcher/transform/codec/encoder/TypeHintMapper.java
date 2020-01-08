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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;

public class TypeHintMapper extends TypeInfoMapper{

	public static final String NON_OBFUSCATED_TYPE_HINT = new String();

	public static Map<String, String> buildBasicTypeHintMap(DexFile dex, TypeClassifier typeClassifier,
			boolean defaultValue) {
		Set<? extends ClassDef> classes = dex.getClasses();
		Map<String, String> typeHintMap = new HashMap<>(classes.size());
		for (ClassDef classDef : classes) {
			String type = classDef.getType();
			boolean obfuscated = typeClassifier.isObfuscatedType(type, defaultValue);
			typeHintMap.put(type, obfuscated ? "" : NON_OBFUSCATED_TYPE_HINT);
		}
		return typeHintMap;
	}

	public static StringBuilder encodeTypeHint(StringBuilder sb, String type, StringEscaper stringEscaper) {
		//if (!DexUtils.isClassDescriptor(type)) throw new AssertionError("Invalid class descriptor");
		int packageEnd = type.lastIndexOf('/');
		int nameStart = (packageEnd < 0) ? 1 : packageEnd + 1;
		stringEscaper.escape(sb.append('_'), type.substring(nameStart, type.length() - 1));
		return sb;
	}

	protected final boolean allowMultipleTypeHints;
	protected final Pattern ignoredHintTypePattern;
	protected final Set<String> ignoredHintTypes;
	protected final StringEscaper stringEscaper;

	public TypeHintMapper(TypeClassifier typeClassifier, boolean allowMultipleTypeHints,
			Pattern ignoredHintTypePattern, Set<String> ignoredHintTypes, StringEscaper stringEscaper) {
		super(typeClassifier, true);
		this.allowMultipleTypeHints = allowMultipleTypeHints;
		this.ignoredHintTypePattern = ignoredHintTypePattern;
		this.ignoredHintTypes = ignoredHintTypes;
		this.stringEscaper = stringEscaper;
	}

	public Map<String, String> buildTypeHintMap(DexFile dex) {
		Map<String, TypeInfoMapper.TypeInfo> typeInfoMap = buildTypeInfoMap(dex);
		Map<String, String> typeHintMap = new HashMap<>(typeInfoMap.size());
		for (Map.Entry<String, TypeInfoMapper.TypeInfo> entry : typeInfoMap.entrySet()) {
			String type = entry.getKey();
			TypeInfoMapper.TypeInfo info = entry.getValue();
			String hint;
			if (info == TypeInfoMapper.NON_OBFUSCATED_TYPE_INFO) {
				hint = NON_OBFUSCATED_TYPE_HINT;
			} else {
				StringBuilder sb = new StringBuilder();
				if (info.clearType != null) {
					encodeTypeHint(sb, info.clearType, stringEscaper);
				} else {
					Set<String> clearInterfaces = info.clearInterfaces;
					if (allowMultipleTypeHints || clearInterfaces.size() == 1) {
						for (String interfaceType : clearInterfaces) {
							encodeTypeHint(sb, interfaceType, stringEscaper);
						}
					}
				}
				hint = sb.toString();
			}
			typeHintMap.put(type, hint);
		}
		return typeHintMap;
	}

	@Override
	public boolean isIgnoredType(String type) {
		return
				(ignoredHintTypes != null &&
						ignoredHintTypes.contains(type)) ||
				(ignoredHintTypePattern != null &&
						ignoredHintTypePattern.matcher(type).region(1, type.length() - 1).matches());
	}

}
