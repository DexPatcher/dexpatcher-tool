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

public final class CopyStringBuilder {

	private static final boolean CHECK_ARGS = true;     // TODO: Disable after testing.

	private final int initialCapacity;
	private final String source;

	private StringBuilder sb;
	private int pos;

	public CopyStringBuilder(String source, int copyPos) {
		this(source.length() + 16, source, copyPos);
	}

	public CopyStringBuilder(int initialCapacity, String source, int copyPos) {
		if (CHECK_ARGS && (copyPos < 0 || copyPos > source.length())) throw new IllegalArgumentException("copyPos");
		this.initialCapacity = initialCapacity;
		this.source = source;
		pos = copyPos;
	}

	public void copyCount(int count) {
		copy(pos + count);
	}

	public void copyToEnd() {
		copy(source.length());
	}

	public void copy(int newPos) {
		if (CHECK_ARGS && (newPos < pos || newPos > source.length())) throw new IllegalArgumentException("newPos");
		if (sb != null) sb.append(source, pos, newPos);
		pos = newPos;
	}

	public StringBuilder skip(int newPos) {
		if (CHECK_ARGS && (newPos < pos || newPos > source.length())) throw new IllegalArgumentException("newPos");
		if (sb == null) sb = new StringBuilder(initialCapacity).append(source, 0, pos);
		pos = newPos;
		return sb;
	}

	public StringBuilder get() {
		if (sb == null) sb = new StringBuilder(initialCapacity).append(source, 0, pos);
		return sb;
	}

	public StringBuilder getOrNull() {
		return sb;
	}

	public String getSource() {
		return source;
	}

	public int getPos() {
		return pos;
	}

}
