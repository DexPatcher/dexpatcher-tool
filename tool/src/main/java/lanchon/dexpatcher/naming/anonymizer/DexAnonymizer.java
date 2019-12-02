/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.naming.anonymizer;

import lanchon.dexpatcher.core.logger.Logger;
import lanchon.dexpatcher.core.util.Label;
import lanchon.dexpatcher.naming.AbstractLoggingRewriter;

import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.Rewriters;

public final class DexAnonymizer extends AbstractLoggingRewriter
		implements Rewriter<String>, TypeAnonymizer.ErrorHandler {

	public static DexFile anonymize(DexFile dex, TypeAnonymizer typeAnonymizer, Logger logger, String logPrefix,
			Logger.Level infoLevel, Logger.Level errorLevel) {
		final DexAnonymizer anonymizer = new DexAnonymizer(typeAnonymizer, logger, logPrefix, infoLevel, errorLevel);
		RewriterModule anonymizerModule = new RewriterModule() {
			@Override
			public Rewriter<String> getTypeRewriter(Rewriters rewriters) {
				return anonymizer;
			}
		};
		return new DexRewriter(anonymizerModule).rewriteDexFile(dex);
	}

	private final TypeAnonymizer typeAnonymizer;
	private final Logger.Level infoLevel;
	private final Logger.Level errorLevel;

	private DexAnonymizer(TypeAnonymizer typeAnonymizer, Logger logger, String logPrefix, Logger.Level infoLevel,
			Logger.Level errorLevel) {
		super(logger, logPrefix);
		this.typeAnonymizer = typeAnonymizer;
		this.infoLevel = infoLevel;
		this.errorLevel = errorLevel;
	}

	@Override
	public String rewrite(String type) {
		String anonymizedType = typeAnonymizer.anonymizeType(type, this);
		if (anonymizedType != type && isLogging(infoLevel) && !anonymizedType.equals(type)) {
			StringBuilder sb = getMessageHeader(type);
			String action = typeAnonymizer.isReanonymizer() ? "reanonymized to '" : "deanonymized to '";
			sb.append(action).append(Label.fromClassDescriptor(anonymizedType)).append("'");
			log(infoLevel, sb.toString());
		}
		return anonymizedType;
	}

	@Override
	public void onError(String type, String message) {
		if (isLogging(errorLevel)) {
			StringBuilder sb = getMessageHeader(type);
			sb.append(message);
			log(errorLevel, sb.toString());
		}
	}

	private StringBuilder getMessageHeader(String type) {
		StringBuilder sb = getBaseMessageHeader();
		sb.append("type '").append(Label.fromClassDescriptor(type)).append("': ");
		return sb;
	}

}
