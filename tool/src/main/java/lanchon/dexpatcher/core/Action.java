/*
 * DexPatcher - Copyright 2015-2020 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core;

import java.util.Locale;

import org.jf.dexlib2.iface.value.EnumEncodedValue;

public enum Action {

	ADD("DexAdd", false),
	EDIT("DexEdit", true),
	REPLACE("DexReplace", false),
	REMOVE("DexRemove", true),
	IGNORE("DexIgnore", true),
	WRAP("DexWrap", false),
	PREPEND("DexPrepend", false),
	APPEND("DexAppend", false),

	NONE(null, false);

	public static final String NAME_UNDEFINED = "UNDEFINED";

	private final String className;
	private final String label;
	private final boolean ignoresCode;

	Action(String className, boolean ignoresCode) {
		this.className = className;
		label = name().toLowerCase(Locale.ROOT);
		this.ignoresCode = ignoresCode;
	}

	public String getClassName() {
		return className;
	}

	public String getLabel() {
		return label;
	}

	public boolean ignoresCode() {
		return ignoresCode;
	}

	public PatchException invalidAction() {
		return new PatchException("invalid action (" + label + ")");
	}

	public static Action fromEnumEncodedValue(EnumEncodedValue value) {
		String s = value.getValue().getName();
		if (NAME_UNDEFINED.equals(s)) return null;
		return Action.valueOf(s);
	}

}
