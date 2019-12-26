/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.anonymizer;

import lanchon.dexpatcher.core.logger.Logger;
import lanchon.dexpatcher.core.util.Label;
import lanchon.dexpatcher.transform.DexTransform;
import lanchon.dexpatcher.transform.TransformLogger;

import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.Rewriters;

public final class DexAnonymizer extends DexTransform implements Rewriter<String>, TypeAnonymizer.ErrorHandler {

	private final TypeAnonymizer typeAnonymizer;
	private final Logger.Level infoLevel;
	private final Logger.Level errorLevel;

	public DexAnonymizer(TypeAnonymizer typeAnonymizer, TransformLogger logger, String logPrefix,
			Logger.Level infoLevel, Logger.Level errorLevel) {
		super(logger, logPrefix);
		this.typeAnonymizer = typeAnonymizer;
		this.infoLevel = infoLevel;
		this.errorLevel = errorLevel;
	}

	@Override
	public RewriterModule getRewriterModule() {
		return new RewriterModule() {
			@Override
			public Rewriter<String> getTypeRewriter(Rewriters rewriters) {
				return DexAnonymizer.this;
			}
		};
	}

	@Override
	protected String getTransformedDefiningClass(String definingClass) {
		return typeAnonymizer.anonymizeType(definingClass);
	}

	@Override
	public String rewrite(String type) {
		String anonymizedType = typeAnonymizer.anonymizeType(type, this);
		if (anonymizedType != type && logger.isLogging(infoLevel) && !anonymizedType.equals(type)) {
			StringBuilder sb = getMessageHeaderForClass(type);
			sb.append(typeAnonymizer.isReanonymizer() ? "reanonymized to '" : "deanonymized to '")
					.append(Label.fromClassDescriptor(anonymizedType)).append("'");
			logger.log(infoLevel, sb.toString());
		}
		return anonymizedType;
	}

	@Override
	public void onError(String type, String message) {
		if (logger.isLogging(errorLevel)) {
			StringBuilder sb = getMessageHeaderForClass(type);
			sb.append(message);
			logger.log(errorLevel, sb.toString());
		}
	}

}
