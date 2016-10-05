/*
 * DexPatcher - Copyright 2015, 2016 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.multidex;

public class BasicDexFileNamer implements DexFileNamer {

	public static final String DEFAULT_PREFIX = "classes";
	public static final String DEFAULT_SUFFIX = ".dex";
	public static final String DEFAULT_BASE_NAME = DEFAULT_PREFIX + DEFAULT_SUFFIX;

	private final String prefix;
	private final String suffix;

	public BasicDexFileNamer() {
		this(DEFAULT_PREFIX, DEFAULT_SUFFIX);
	}

	public BasicDexFileNamer(String baseName) {
		if (!baseName.endsWith(DEFAULT_SUFFIX)) throw new IllegalArgumentException("Invalid dex file base name");
		prefix = baseName.substring(0, baseName.length() - DEFAULT_SUFFIX.length());
		suffix = DEFAULT_SUFFIX;
	}

	public BasicDexFileNamer(String prefix, String suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}

	@Override
	public String getName(int index) {
		if (index < 0) throw new IllegalArgumentException("index");
		return prefix + (index > 0 ? index + 1 : "") + suffix;
	}

	@Override
	public boolean isValidName(String name) {
		return name.startsWith(prefix) && name.endsWith(suffix);
	}

}
