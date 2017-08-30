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

public enum Action {

	ADD(Marker.ADD),
	EDIT(Marker.EDIT),
	REPLACE(Marker.REPLACE),
	REMOVE(Marker.REMOVE),
	IGNORE(Marker.IGNORE),
	WRAP(Marker.WRAP),
	APPEND(Marker.APPEND);

	private final Marker marker;
	private final String label;

	Action(Marker marker) {
		this.marker = marker;
		label = name().toLowerCase();
	}

	public Marker getMarker() {
		return marker;
	}

	public String getLabel() {
		return label;
	}

	public PatchException invalidAction() {
		return new PatchException("invalid action (" + label + ")");
	}

}
