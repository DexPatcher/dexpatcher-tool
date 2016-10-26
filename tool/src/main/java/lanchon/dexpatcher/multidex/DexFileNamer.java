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

import java.util.NoSuchElementException;

public interface DexFileNamer {

	String getName(int index);
	int getIndex(String name);
	boolean isValidName(String name);

	class Comparator implements java.util.Comparator<String> {

		private final DexFileNamer namer;

		public Comparator(DexFileNamer namer) {
			this.namer = namer;
		}

		@Override
		public int compare(String l, String r) {
			int li = namer.getIndex(l);
			int ri = namer.getIndex(r);
			boolean lv = (li >= 0);
			boolean rv = (ri >= 0);
			if (lv != rv) return lv ? -1 : 1;
			if (!lv) return l.compareTo(r);
			return li < ri ? -1 : (li > ri ? 1 : 0);
		}

	}

	class Iterator implements java.util.Iterator<String> {

		public static int NO_MAX_COUNT = -1;

		private final DexFileNamer namer;
		private final int maxCount;
		private int count;

		public Iterator(DexFileNamer namer) {
			this.namer = namer;
			this.maxCount = NO_MAX_COUNT;
		}

		public Iterator(DexFileNamer namer, int maxCount) {
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

}
