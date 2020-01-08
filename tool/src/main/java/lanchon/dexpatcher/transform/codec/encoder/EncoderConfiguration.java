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

import java.util.Set;
import java.util.regex.Pattern;

public class EncoderConfiguration extends StringEscaperConfiguration {

	public Pattern obfuscatedBinaryTypeNamePattern;

	public Pattern obfuscatedPackageNamePattern;
	public Pattern obfuscatedClassNamePattern;
	public Pattern obfuscatedMemberNamePattern;

	//public boolean encodeAllPackages;
	public boolean encodeAllClasses;
	//public boolean encodeAllMembers;

	public boolean encodeObfuscatedPackages;
	public boolean encodeObfuscatedClasses;
	public boolean encodeObfuscatedMembers;

	public boolean encodeReservedCharacters;
	public boolean encodeReservedWords;

	public boolean encodeTypeHintsInClasses;
	public boolean encodeTypeHintsInMembers;
	public boolean encodeTypeInfoInMembers;

	public boolean includeIdentifierType = true;
	public boolean allowMultipleTypeHints = true;
	public boolean processNestedClasses = true;

	public Pattern ignoredHintTypePattern;
	public Set<String> ignoredHintTypes = DefaultIgnoredHintTypes.SET;

	public void setEncodeCompilable() {
		encodeAllClasses = true;
		encodeReservedCharacters = true;
		encodeReservedWords = true;
		encodeTypeInfoInMembers = true;
		includeIdentifierType = true;
	}

}
