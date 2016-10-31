/*
 * DexPatcher - Copyright 2015, 2016 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.multidexlib2;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DexFileNameIterator implements Iterator<String> {

	public static int NO_MAX_COUNT = -1;

	private final DexFileNamer namer;
	private final int maxCount;
	private int count;

	public DexFileNameIterator(DexFileNamer namer) {
		this(namer, NO_MAX_COUNT);
	}

	public DexFileNameIterator(DexFileNamer namer, int maxCount) {
		this.namer = namer;
		this.maxCount = maxCount;
	}

	@Override
	public boolean hasNext() {
		return maxCount < 0 || count < maxCount;
	}

	@Override
	public String next() {
		if (!hasNext()) throw new NoSuchElementException();
		return namer.getName(count++);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public DexFileNamer getNamer() {
		return namer;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public int getCount() {
		return count;
	}

}
