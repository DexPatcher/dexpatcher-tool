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

import java.util.Map;
import java.util.regex.Pattern;

import lanchon.dexpatcher.core.util.DexUtils;
import lanchon.dexpatcher.core.util.InvalidTypeDescriptorException;
import lanchon.dexpatcher.core.util.TypeName;
import lanchon.dexpatcher.transform.mapper.map.DexMap;

import com.google.common.base.Splitter;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;

public class EncoderDexMap implements DexMap {

	protected static final String TYPE_HINT_HEADER = "_H";

	private static final int CLASS_INITIAL_CAPACITY = 64 - 8;
	protected static final int MEMBER_INITIAL_CAPACITY = 64 - 8;

	protected final String codeMarker;
	protected final DexMap encodeMap;

	// From EncoderConfiguration:

	protected final Pattern obfuscatedPackageNamePattern;
	protected final Pattern obfuscatedMemberNamePattern;

	//protected final boolean encodeAllPackages;
	protected final boolean encodeAllClasses;
	//protected final boolean encodeAllMembers;

	protected final boolean encodeObfuscatedPackages;
	protected final boolean encodeObfuscatedClasses;
	protected final boolean encodeObfuscatedMembers;

	protected final boolean encodeReservedCharacters;
	protected final boolean encodeReservedWords;

	protected final boolean encodeTypeHintsInClasses;
	protected final boolean encodeTypeHintsInMembers;
	protected final boolean encodeTypeInfoInMembers;

	protected final boolean includeIdentifierType;
	protected final boolean processNestedClasses;

	// Generated:

	protected final StringEscaper stringEscaper;

	protected final TypeHintMapper typeHintMapper;
	protected final Map<String, String> typeHintMap;

	protected final boolean processPackageNames;
	protected final boolean processClassNames;

	public EncoderDexMap(DexFile dex, String codeMarker, DexMap encodeMap,
			EncoderConfiguration configuration) {

		this.codeMarker = codeMarker;
		this.encodeMap = encodeMap;

		this.obfuscatedPackageNamePattern = configuration.obfuscatedPackageNamePattern;
		this.obfuscatedMemberNamePattern = configuration.obfuscatedMemberNamePattern;
		//this.encodeAllPackages = configuration.encodeAllPackages;
		this.encodeAllClasses = configuration.encodeAllClasses;
		//this.encodeAllMembers = configuration.encodeAllMembers;
		this.encodeObfuscatedPackages = configuration.encodeObfuscatedPackages;
		this.encodeObfuscatedClasses = configuration.encodeObfuscatedClasses;
		this.encodeObfuscatedMembers = configuration.encodeObfuscatedMembers;
		this.encodeReservedCharacters = configuration.encodeReservedCharacters;
		this.encodeReservedWords = configuration.encodeReservedWords;
		this.encodeTypeHintsInClasses = configuration.encodeTypeHintsInClasses;
		this.encodeTypeHintsInMembers = configuration.encodeTypeHintsInMembers;
		this.encodeTypeInfoInMembers = configuration.encodeTypeInfoInMembers;
		this.includeIdentifierType = configuration.includeIdentifierType;
		this.processNestedClasses = configuration.processNestedClasses;

		this.stringEscaper = new StringEscaper(configuration);
		TypeClassifier typeClassifier = new TypeClassifier(configuration.obfuscatedBinaryTypeNamePattern,
				configuration.obfuscatedClassNamePattern);
		if (encodeTypeHintsInClasses || encodeTypeHintsInMembers) {
			TypeHintMapper typeHintMapper = new TypeHintMapper(typeClassifier, configuration.allowMultipleTypeHints,
					configuration.ignoredHintTypePattern, configuration.ignoredHintTypes, stringEscaper);
			this.typeHintMapper = encodeTypeHintsInMembers ? typeHintMapper : null;
			typeHintMap = typeHintMapper.buildTypeHintMap(dex);
		} else {
			typeHintMapper = null;
			typeHintMap = TypeHintMapper.buildBasicTypeHintMap(dex, typeClassifier, true);
		}
		processPackageNames =
				//(encodeAllPackages) ||
				(encodeObfuscatedPackages) ||
				(encodeReservedCharacters) ||
				(encodeReservedWords);
		processClassNames =
				(encodeMap != null) ||
				(encodeAllClasses) ||
				(encodeObfuscatedClasses) ||
				(encodeReservedCharacters) ||
				(encodeReservedWords) ||
				(encodeTypeHintsInClasses);

	}

	// Classes

	@Override
	public String getClassMapping(String descriptor) {
		//if (!DexUtils.isClassDescriptor(descriptor)) throw new AssertionError("Invalid class descriptor");
		String typeHint = typeHintMap.get(descriptor);
		if (typeHint == null) return null;      // even if outer classes are present in the dex (for speed)
		CopyStringBuilder csb = new CopyStringBuilder(CLASS_INITIAL_CAPACITY, descriptor, 1);
		int packageEnd = descriptor.lastIndexOf('/');
		int nameStart = (packageEnd < 0) ? 1 : packageEnd + 1;
		if (processPackageNames) {
			encodePackage(csb, nameStart);
		} else {
			csb.copy(nameStart);
		}
		if (processClassNames && !DexUtils.isPackageDescriptor(descriptor)) {
			encodeClass(csb, nameStart, typeHint);
		}
		csb.copyToEnd();
		StringBuilder sb = csb.getOrNull();
		//if (sb != null && descriptor.contentEquals(sb)) throw new AssertionError("Identity class mapping");
		return (sb != null) ? sb.toString() : null;
	}

	// Package Name

	protected final void encodePackage(CopyStringBuilder csb, int nameStart) {
		String descriptor = csb.getSource();
		int componentStart = 1;     // = csb.getPos();
		while (componentStart != nameStart) {
			int componentEnd = descriptor.indexOf('/', componentStart);
			if (componentStart != componentEnd) {
				encodePackageComponent(csb, componentStart, componentEnd);
			}
			componentStart = componentEnd + 1;
			csb.copy(componentStart);
		}
	}

	protected final void encodePackageComponent(CopyStringBuilder csb, int componentStart, int componentEnd) {
		String name = csb.getSource().substring(componentStart, componentEnd);
		boolean encode =
				//(encodeAllPackages) ||
				(encodeObfuscatedPackages && (obfuscatedPackageNamePattern == null ||
						obfuscatedPackageNamePattern.matcher(name).matches())) ||
				(encodeReservedCharacters && stringEscaper.shouldEscape(name)) ||
				(encodeReservedWords && ReservedWords.isReserved(name));
		if (encode) {
			StringBuilder sb = csb.skip(componentEnd);
			sb.append('_');
			if (includeIdentifierType) sb.append("_p");
			stringEscaper.escape(sb.append(codeMarker), name).append("__");
		} else {
			csb.copy(componentEnd);
		}
	}

	// Class Name

	private static final Splitter DESCRIPTOR_SPLITTER = Splitter.on('/').omitEmptyStrings();

	protected final void encodeClass(CopyStringBuilder csb, int nameStart, String typeHint) {
		String descriptor = csb.getSource();
		int nameEnd = descriptor.length() - 1;
		int nestingLevel = 0;
		int componentStart = nameStart;
		if (processNestedClasses) {
			int componentEndLimit = nameEnd - 1;
			for (int componentEnd = nameStart + 1; componentEnd < componentEndLimit; componentEnd++) {
				if (descriptor.charAt(componentEnd) == '$') {
					String componentDescriptor = new StringBuilder(componentEnd + 1)
							.append(descriptor, 0, componentEnd).append(';').toString();
					String componentTypeHint = typeHintMap.get(componentDescriptor);
					if (componentTypeHint != null) {
						encodeClassComponent(csb, nameStart, nestingLevel, componentDescriptor, componentStart,
								componentEnd, componentTypeHint);
						componentEnd++;
						componentStart = componentEnd;
						csb.copy(componentStart);
						nestingLevel ++;
					}
				}
			}
		}
		encodeClassComponent(csb, nameStart, nestingLevel, descriptor, componentStart, nameEnd, typeHint);
	}

	protected final void encodeClassComponent(CopyStringBuilder csb, int nameStart, int nestingLevel,
			String componentDescriptor, int componentStart, int componentEnd, String componentTypeHint) {
		String componentMapping = (encodeMap != null) ? encodeMap.getClassMapping(componentDescriptor) : null;
		if (componentDescriptor.equals(componentMapping)) componentMapping = null;
		String componentName = componentDescriptor.substring(componentStart, componentEnd);
		//noinspection StringEquality
		boolean encode =
				(componentMapping != null) ||
				(encodeAllClasses) ||
				(encodeObfuscatedClasses && componentTypeHint != TypeHintMapper.NON_OBFUSCATED_TYPE_HINT) ||
				(encodeTypeHintsInClasses && !componentTypeHint.isEmpty()) ||
				(encodeReservedCharacters && stringEscaper.shouldEscape(componentName)) ||
				(encodeReservedWords && ReservedWords.isReserved(componentName));
		if (encode) {
			StringBuilder sb = csb.skip(componentEnd);
			sb.append('_');
			if (includeIdentifierType) {
				sb.append("_C");
				if (nestingLevel != 0) sb.append(nestingLevel);
			}
			if (componentMapping != null) encodeClassMapping(sb, nameStart, componentDescriptor, componentMapping);
			if (encodeTypeHintsInClasses && !componentTypeHint.isEmpty()) {
				sb.append(TYPE_HINT_HEADER).append(componentTypeHint);
			}
			stringEscaper.escape(sb.append(codeMarker), componentName).append("__");
		} else {
			csb.copy(componentEnd);
		}
	}

	// Members

	@Override
	public String getFieldMapping(FieldReference field) {
		if (!typeHintMap.containsKey(field.getDefiningClass())) return null;
		String mapping = (encodeMap != null) ? encodeMap.getFieldMapping(field) : null;
		return getMemberMapping("_f", field.getName(), mapping, field.getType());
	}

	@Override
	public String getMethodMapping(MethodReference method) {
		if (!typeHintMap.containsKey(method.getDefiningClass())) return null;
		if (DexUtils.isStaticConstructorReference(method) || DexUtils.isInstanceConstructorReference(method)) {
			return null;
		}
		String mapping = (encodeMap != null) ? encodeMap.getMethodMapping(method) : null;
		return getMemberMapping("_m", method.getName(), mapping, method.getReturnType());
	}

	protected final String getMemberMapping(String identifierType, String name, String mapping, String type) {
		if (name.equals(mapping)) mapping = null;
		StringBuilder typeHint = encodeTypeHintsInMembers ? encodeMemberTypeHint(null, type) : null;
		boolean obfuscated = (encodeObfuscatedMembers || typeHint != null || encodeTypeInfoInMembers) &&
				(obfuscatedMemberNamePattern == null || obfuscatedMemberNamePattern.matcher(name).matches());
		boolean encode =
				(mapping != null) ||
				//(encodeAllMembers) ||
				(encodeObfuscatedMembers && obfuscated) ||
				(typeHint != null && obfuscated) ||
				(encodeTypeInfoInMembers && obfuscated) ||
				(encodeReservedCharacters && stringEscaper.shouldEscape(name)) ||
				(encodeReservedWords && ReservedWords.isReserved(name));
		if (encode) {
			StringBuilder sb = new StringBuilder(MEMBER_INITIAL_CAPACITY).append('_');
			if (includeIdentifierType) sb.append(identifierType);
			if (mapping != null) stringEscaper.escape(sb.append('_'), mapping);
			if (typeHint != null && obfuscated) sb.append(TYPE_HINT_HEADER).append(typeHint);
			if (encodeTypeInfoInMembers && obfuscated) encodeMemberTypeInfo(sb, type);
			stringEscaper.escape(sb.append(codeMarker), name).append("__");
			return sb.toString();
		} else {
			return null;
		}
	}

	// Helpers

	private static final Splitter TYPE_NAME_SPLITTER = Splitter.on('.').omitEmptyStrings();

	protected final StringBuilder encodeClassMapping(StringBuilder sb, int nameStart, String descriptor,
			String mapping) {
		int mappingNameStart = 1;
		boolean encodeMappingPackage = false;
		int mappingPackageEnd = mapping.lastIndexOf('/');
		if (mappingPackageEnd >= 0) {
			mappingNameStart = mappingPackageEnd + 1;
			encodeMappingPackage = !(nameStart == mappingNameStart &&
					//mapping.regionMatches(0, descriptor, 0, nameStart));
					mapping.regionMatches(1, descriptor, 1, mappingPackageEnd));
		}
		if (encodeMappingPackage) {
			for (String component : DESCRIPTOR_SPLITTER.split(mapping.substring(1, mappingPackageEnd))) {
				stringEscaper.escape(sb.append('_'), component);
			}
		}
		stringEscaper.escape(sb.append('_'), mapping.substring(mappingNameStart, mapping.length() - 1));
		return sb;
	}

	protected final StringBuilder encodeMemberTypeHint(StringBuilder sb, String type) {
		if (!type.endsWith(";")) return sb;
		int array = 0;
		while (type.charAt(array) == '[') array++;
		if (array != 0) type = type.substring(array);
		if (!type.startsWith("L")) return sb;
		String typeHint = typeHintMap.get(type);
		//noinspection StringEquality
		if (typeHint == null || typeHint == TypeHintMapper.NON_OBFUSCATED_TYPE_HINT) {
			if (!typeHintMapper.isIgnoredType(type)) {
				if (sb == null) sb = new StringBuilder(MEMBER_INITIAL_CAPACITY);
				if (array != 0) sb.append("_A").append(array);
				TypeHintMapper.encodeTypeHint(sb, type, stringEscaper);
			}
		} else if (!typeHint.isEmpty()) {
			if (sb == null) sb = new StringBuilder(MEMBER_INITIAL_CAPACITY);
			if (array != 0) sb.append("_A").append(array);
			sb.append(typeHint);
		}
		return sb;
	}

	protected final StringBuilder encodeMemberTypeInfo(StringBuilder sb, String type) {
		String typeName;
		try {
			typeName = TypeName.fromReturnDescriptor(type);
		} catch (InvalidTypeDescriptorException e) {
			stringEscaper.escape(sb.append("_TYPE$_"), type);
			return sb;
		}
		int array = 0;
		int typeNameEnd = typeName.length();
		while (typeName.startsWith("[]", typeNameEnd - 2)) { array++; typeNameEnd -= 2; }
		if (array != 0) {
			typeName = typeName.substring(0, typeNameEnd);
			sb.append("_A").append(array);
		}
		if (type.endsWith(";")) sb.append("_$");
		//if (typeName.startsWith(".")) sb.append("_$");
		for (String component : TYPE_NAME_SPLITTER.split(typeName)) stringEscaper.escape(sb.append('_'), component);
		//if (typeName.startsWith(".")) sb.append('$');
		return sb;
	}

}
