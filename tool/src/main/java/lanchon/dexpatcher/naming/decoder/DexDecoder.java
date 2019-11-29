/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.naming.decoder;

import java.util.HashSet;

import lanchon.dexpatcher.core.logger.Logger;
import lanchon.dexpatcher.core.util.Label;
import lanchon.dexpatcher.naming.decoder.DexDecoderModule.ItemType;

import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.rewriter.DexRewriter;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public class DexDecoder implements DexDecoderModule.ItemRewriter {

	private static final boolean LOG_DECODED_TYPES = false;

	private class ErrorHandler implements NameDecoder.ErrorHandler {

		private final String definingClass;
		private final ItemType itemType;
		private final String value;

		public ErrorHandler(String definingClass, ItemType itemType, String value) {
			this.definingClass = definingClass;
			this.itemType = itemType;
			this.value = value;
		}

		@Override
		public void onError(String message, String string, int codeStart, int codeEnd, int errorStart, int errorEnd) {
			// NOTE: This call to logger.isLogging() is not synchronized.
			if (errorLevel != NONE && logger.isLogging(errorLevel)) {
				StringBuilder sb = buildMessage();
				sb.append(message);
				sb.append(" in '").append(string, codeStart, errorStart)
						.append("[->]").append(string, errorStart, errorEnd).append("[<-]")
						.append(string, errorEnd, codeEnd).append("'");
				log(errorLevel, sb.toString());
			}
		}

		public final StringBuilder buildMessage() {
			StringBuilder sb = new StringBuilder();
			if (logPrefix != null) sb.append(logPrefix).append(": ");
			if (definingClass != null) {
				sb.append("type '").append(Label.fromClassDescriptor(definingClass));
				if (LOG_DECODED_TYPES) {
					String decodedDefiningClass = nameDecoder.decode(definingClass);
					if (!definingClass.equals(decodedDefiningClass)) {
						sb.append("' -> '").append(Label.fromClassDescriptor(decodedDefiningClass));
					}
				}
				sb.append("': ");
			}
			sb.append(itemType.label).append(" '").append(formatValue(value)).append("': ");
			return sb;
		}

		public final String formatValue(String value) {
			return itemType == ItemType.NAKED_TYPE_NAME ? value.replace('/', '.') : value;
		}

	}

	@Override
	public String rewriteItem(String definingClass, ItemType itemType, String value) {
		ErrorHandler errorHandler = new ErrorHandler(definingClass, itemType, value);
		String decodedValue = nameDecoder.decode(value, errorHandler);
		if (decodedValue != value && decodedValue != null) {
			// NOTE: This call to logger.isLogging() is not synchronized.
			if (infoLevel != NONE && logger.isLogging(infoLevel) && !decodedValue.equals(value)) {
				StringBuilder sb = errorHandler.buildMessage();
				sb.append("decoded to '").append(errorHandler.formatValue(decodedValue)).append("'");
				log(infoLevel, sb.toString());
			}
		}
		return decodedValue;
	}

	private final NameDecoder nameDecoder;
	private final Logger logger;
	private final String logPrefix;
	private final Logger.Level infoLevel;
	private final Logger.Level errorLevel;

	private final HashSet<String> loggedMessages = new HashSet<>();

	private DexDecoder(NameDecoder nameDecoder, Logger logger, String logPrefix, Logger.Level infoLevel,
			Logger.Level errorLevel) {
		this.nameDecoder = nameDecoder;
		this.logger = logger;
		this.logPrefix = logPrefix;
		this.infoLevel = infoLevel;
		this.errorLevel = errorLevel;
	}

	private synchronized void log(Logger.Level level, String message) {
		if (logger.isLogging(level) && loggedMessages.add(message)) logger.log(level, message);
	}

	public static DexFile decode(DexFile dex, NameDecoder nameDecoder, Logger logger, String logPrefix,
			Logger.Level infoLevel, Logger.Level errorLevel) {
		DexDecoder decoder = new DexDecoder(nameDecoder, logger, logPrefix, infoLevel, errorLevel);
		return new DexRewriter(new DexDecoderModule(decoder)).rewriteDexFile(dex);
	}

}
