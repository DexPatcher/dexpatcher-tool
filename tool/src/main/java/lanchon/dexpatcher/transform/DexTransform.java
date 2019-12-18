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
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.rewriter.RewriterModule;

public abstract class DexTransform {

	public interface Transform extends DexFile {
		DexFile getSourceDexFile();
		void stopLogging();
	}

	protected class TransformRewriter extends DexRewriter {
		protected class TransformedDexFile extends RewrittenDexFile implements Transform {
			public TransformedDexFile(DexFile dex)
			{
				super(dex);
			}
			@Override
			public DexFile getSourceDexFile() {
				return dexFile;
			}
			@Override
			public void stopLogging() {
				DexTransform.this.stopLogging(this);
			}
		}
		public TransformRewriter(RewriterModule module) {
			super(module);
		}
		@Override
		public DexFile rewriteDexFile(DexFile dex) {
			return new TransformedDexFile(dex);
		}
	}

	public static void stopLogging(DexFile dex) {
		if (dex instanceof Transform) {
			((Transform) dex).stopLogging();
		}
	}

	public void stopLogging(Transform dex) {
		DexTransform.stopLogging(dex.getSourceDexFile());
	}

	protected DexFile transformDexFile(DexFile dex, RewriterModule module) {
		return new TransformRewriter(module).rewriteDexFile(dex);
	}

}
