/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.codec.decoder;

import lanchon.dexpatcher.core.logger.Logger;
import lanchon.dexpatcher.transform.DexProvider;
import lanchon.dexpatcher.transform.RewriterDexTransform;
import lanchon.dexpatcher.transform.codec.DexCodecModule;
import lanchon.dexpatcher.transform.codec.DexCodecModule.ItemType;

public final class DexDecoder extends RewriterDexTransform implements DexCodecModule.ItemRewriter {

	public static DexProvider decode(DexProvider source, StringDecoder stringDecoder, Logger logger, String logPrefix,
			Logger.Level infoLevel, Logger.Level errorLevel) {
		DexDecoder decoder = new DexDecoder(stringDecoder, logger, logPrefix, infoLevel, errorLevel);
		return decoder.rewriteDex(source, new DexCodecModule(decoder));
	}

	private final class ErrorHandler extends MemberContext implements StringDecoder.ErrorHandler {

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
			return stringDecoder.decodeString(definingClass);
		}

	}

	private final StringDecoder stringDecoder;
	private final Logger.Level infoLevel;
	private final Logger.Level errorLevel;

	private DexDecoder(StringDecoder stringDecoder, Logger logger, String logPrefix, Logger.Level infoLevel,
			Logger.Level errorLevel) {
		super(logger, logPrefix);
		this.stringDecoder = stringDecoder;
		this.infoLevel = infoLevel;
		this.errorLevel = errorLevel;
	}

	@Override
	public String rewriteItem(String definingClass, ItemType itemType, String value) {
		ErrorHandler errorHandler = new ErrorHandler(definingClass, itemType, value);
		String decodedValue = stringDecoder.decodeString(value, errorHandler);
		if (decodedValue != value && isLogging(infoLevel) && !decodedValue.equals(value)) {
			StringBuilder sb = errorHandler.getMessageHeader();
			sb.append("decoded to '").append(errorHandler.formatValue(decodedValue)).append("'");
			log(infoLevel, sb.toString());
		}
		return decodedValue;
	}

}
