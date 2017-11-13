/*
 * DexPatcher - Copyright 2015-2017 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core;

import java.util.Locale;

public enum Action {

	ADD(Marker.ADD, false),
	EDIT(Marker.EDIT, true),
	REPLACE(Marker.REPLACE, false),
	REMOVE(Marker.REMOVE, true),
	IGNORE(Marker.IGNORE, true),
	WRAP(Marker.WRAP, false),
	PREPEND(Marker.PREPEND, false),
	APPEND(Marker.APPEND, false);

	private final Marker marker;
	private final String label;
	private final boolean ignoresCode;

	Action(Marker marker, boolean ignoresCode) {
		this.marker = marker;
		label = name().toLowerCase(Locale.ENGLISH);
		this.ignoresCode = ignoresCode;
	}

	public Marker getMarker() {
		return marker;
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

}
