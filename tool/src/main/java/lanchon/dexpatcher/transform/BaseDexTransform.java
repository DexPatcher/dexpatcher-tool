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

public abstract class BaseDexTransform {

	protected class TransformedDexProvider extends DexProvider.FromTransform {
		public TransformedDexProvider(DexFile dexFile, DexProvider... sources) {
			super(dexFile, sources);
		}
		@Override
		public boolean isLogging() {
			return BaseDexTransform.this.isLogging(this) || super.isLogging();
		}
		@Override
		public void stopLogging() {
			BaseDexTransform.this.stopLogging(this);
			super.stopLogging();
		}
	}

	protected abstract boolean isLogging(TransformedDexProvider dex);
	protected abstract void stopLogging(TransformedDexProvider dex);

}
