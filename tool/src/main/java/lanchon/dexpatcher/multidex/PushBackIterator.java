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

import java.util.Iterator;

public final class PushBackIterator<T> implements Iterator<T> {

	private final Iterator<T> iterator;
	private T lastItem;
	private boolean pushedBack;

	public PushBackIterator(Iterator<T> iterator) {
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		return pushedBack || iterator.hasNext();
	}

	@Override
	public T next() {
		if (pushedBack) {
			pushedBack = false;
		} else {
			lastItem = iterator.next();
		}
		return lastItem;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public void pushBack() {
		// WARNING: Will not detect if no item has ever been read.
		if (pushedBack) throw new IllegalStateException();
		pushedBack = true;
	}

}
