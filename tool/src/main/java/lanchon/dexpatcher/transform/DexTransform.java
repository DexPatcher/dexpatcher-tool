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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.rewriter.RewriterModule;

public abstract class DexTransform {

	public interface Transform extends DexFile {
		DexFile getSourceDexFile();
	}

	protected static class TransformRewriter extends DexRewriter {
		protected class Rewritten extends RewrittenDexFile implements Transform {
			public Rewritten(DexFile dex) { super(dex); }
			@Override public DexFile getSourceDexFile() { return dexFile; }
		}
		public TransformRewriter(RewriterModule module) { super(module); }
		@Override public DexFile rewriteDexFile(DexFile dex) { return new Rewritten(dex); }
	}

	protected DexFile transformDexFile(DexFile dex, RewriterModule module) {
		return new TransformRewriter(module).rewriteDexFile(dex);
	}

	public static Iterable<Transform> getTransforms(final DexFile dex) {
		return new Iterable<Transform>() {
			@Override public Iterator<Transform> iterator() {
				return new Iterator<Transform>() {
					DexFile next = dex;
					@Override public boolean hasNext() {
						return next instanceof Transform;
					}
					@Override public Transform next() {
						if (next instanceof Transform) {
							Transform t = (Transform) next;
							next = t.getSourceDexFile();
							return t;
						} else {
							throw new NoSuchElementException();
						}
					}
					@Override public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

}
