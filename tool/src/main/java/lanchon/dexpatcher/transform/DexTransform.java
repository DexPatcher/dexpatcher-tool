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

import lanchon.dexpatcher.core.util.Label;

import org.jf.dexlib2.rewriter.RewriterModule;

public abstract class DexTransform extends BaseDexTransform {

	private static final boolean LOG_REWRITTEN_TYPES = false;

	protected class MemberContext {
		protected final String definingClass;
		public MemberContext(String definingClass) {
			this.definingClass = definingClass;
		}
	}

	public DexTransform(TransformLogger logger, String logPrefix) {
		super(logger, logPrefix);
	}

	public abstract RewriterModule getRewriterModule();

	public final StringBuilder getMessageHeader(String definingClass) {
		StringBuilder sb = getMessageHeader();
		if (definingClass != null) {
			sb.append("type '").append(Label.fromClassDescriptor(definingClass));
			if (LOG_REWRITTEN_TYPES) {
				String rewrittenDefiningClass = getRewrittenDefiningClass(definingClass);
				if (rewrittenDefiningClass != null && !rewrittenDefiningClass.equals(definingClass)) {
					sb.append("' -> '").append(Label.fromClassDescriptor(rewrittenDefiningClass));
				}
			}
			sb.append("': ");
		}
		return sb;
	}

	protected abstract String getRewrittenDefiningClass(String definingClass);

}
