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

	private final Iterator<? extends T> iterator;
	private boolean hasPushBackItem;
	private T pushBackItem;

	public PushBackIterator(Iterator<? extends T> iterator) {
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		return hasPushBackItem || iterator.hasNext();
	}

	@Override
	public T next() {
		if (hasPushBackItem) {
			T item = pushBackItem;
			hasPushBackItem = false;
			pushBackItem = null;
			return item;
		} else {
			return iterator.next();
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public void pushBack(T item) {
		if (hasPushBackItem) throw new IllegalStateException("Pending pushed back item");
		hasPushBackItem = true;
		pushBackItem = item;
	}

}
