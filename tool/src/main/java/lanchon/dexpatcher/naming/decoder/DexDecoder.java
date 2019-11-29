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

import lanchon.dexpatcher.core.logger.Logger;
import lanchon.dexpatcher.naming.AbstractLoggingRewriter;
import lanchon.dexpatcher.naming.decoder.DexDecoderModule.ItemType;

import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.rewriter.DexRewriter;

public class DexDecoder extends AbstractLoggingRewriter implements DexDecoderModule.ItemRewriter {

	public static DexFile decode(DexFile dex, NameDecoder nameDecoder, Logger logger, String logPrefix,
			Logger.Level infoLevel, Logger.Level errorLevel) {
		DexDecoder decoder = new DexDecoder(nameDecoder, logger, logPrefix, infoLevel, errorLevel);
		return new DexRewriter(new DexDecoderModule(decoder)).rewriteDexFile(dex);
	}

	private final class ErrorHandler extends MemberContext implements NameDecoder.ErrorHandler {

		private final ItemType itemType;
		private final String value;

		public ErrorHandler(String definingClass, ItemType itemType, String value) {
			super(definingClass);
			this.itemType = itemType;
			this.value = value;
		}

		@Override
		public void onError(String message, String string, int codeStart, int codeEnd, int errorStart, int errorEnd) {
			if (isLogging(errorLevel)) {
				StringBuilder sb = getMessageHeader();
				sb.append(message);
				sb.append(" in '").append(string, codeStart, errorStart)
						.append("[->]").append(string, errorStart, errorEnd).append("[<-]")
						.append(string, errorEnd, codeEnd).append("'");
				log(errorLevel, sb.toString());
			}
		}

		@Override
		public StringBuilder getMessageHeader() {
			StringBuilder sb = super.getMessageHeader();
			sb.append(itemType.label).append(" '").append(formatValue(value)).append("': ");
			return sb;
		}

		public String formatValue(String value) {
			return itemType == ItemType.NAKED_TYPE_NAME ? value.replace('/', '.') : value;
		}

		@Override
		protected String getRewrittenDefiningClass() {
			return nameDecoder.decode(definingClass);
		}

	}

	private final NameDecoder nameDecoder;
	private final Logger.Level infoLevel;
	private final Logger.Level errorLevel;

	private DexDecoder(NameDecoder nameDecoder, Logger logger, String logPrefix, Logger.Level infoLevel,
			Logger.Level errorLevel) {
		super(logger, logPrefix);
		this.nameDecoder = nameDecoder;
		this.infoLevel = infoLevel;
		this.errorLevel = errorLevel;
	}

	@Override
	public String rewriteItem(String definingClass, ItemType itemType, String value) {
		ErrorHandler errorHandler = new ErrorHandler(definingClass, itemType, value);
		String decodedValue = nameDecoder.decode(value, errorHandler);
		if (decodedValue != value && isLogging(infoLevel) && !decodedValue.equals(value)) {
			StringBuilder sb = errorHandler.getMessageHeader();
			sb.append("decoded to '").append(errorHandler.formatValue(decodedValue)).append("'");
			log(infoLevel, sb.toString());
		}
		return decodedValue;
	}

}
