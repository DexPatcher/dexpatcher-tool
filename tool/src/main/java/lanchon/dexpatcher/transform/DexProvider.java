/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform;

import org.jf.dexlib2.iface.DexFile;

public interface DexProvider {

	DexFile getDexFile();

	boolean isLogging();
	void stopLogging();

	class FromDexFile implements DexProvider {

		protected final DexFile dexFile;

		public FromDexFile(DexFile dexFile) {
			this.dexFile = dexFile;
		}

		@Override
		public DexFile getDexFile() {
			return dexFile;
		}

		@Override
		public boolean isLogging() {
			return false;
		}

		@Override
		public void stopLogging() {}

	}

	class FromTransform extends FromDexFile {

		protected final DexProvider[] sources;

		public FromTransform(DexFile dexFile, DexProvider... sources) {
			super(dexFile);
			this.sources = sources;
		}

		@Override
		public boolean isLogging() {
			for (DexProvider source : sources) {
				if (source.isLogging()) return true;
			}
			return false;
		}

		@Override
		public void stopLogging() {
			for (DexProvider source : sources) {
				source.stopLogging();
			}
		}

	}

}
