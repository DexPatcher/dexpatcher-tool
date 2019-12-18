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
import lanchon.dexpatcher.transform.TransformLogger;
import lanchon.dexpatcher.transform.codec.DexCodec;
import lanchon.dexpatcher.transform.codec.DexCodecModule.ItemType;

public final class DexDecoder extends DexCodec {

	private final class ErrorHandler extends MemberContext implements StringDecoder.ErrorHandler {

		public ErrorHandler(String definingClass, ItemType itemType, String value) {
			super(definingClass, itemType, value);
		}

		@Override
		public void onError(String message, String string, int codeStart, int codeEnd, int errorStart, int errorEnd) {
			if (logger.isLogging(errorLevel)) {
				StringBuilder sb = getMessageHeader(definingClass, itemType, value);
				sb.append(message);
				sb.append(" in '").append(string, codeStart, errorStart)
						.append("[->]").append(string, errorStart, errorEnd).append("[<-]")
						.append(string, errorEnd, codeEnd).append("'");
				logger.log(errorLevel, sb.toString());
			}
		}

	}

	private final StringDecoder stringDecoder;
	private final Logger.Level infoLevel;
	private final Logger.Level errorLevel;

	public DexDecoder(StringDecoder stringDecoder, TransformLogger logger, String logPrefix, Logger.Level infoLevel,
			Logger.Level errorLevel) {
		super(logger, logPrefix);
		this.stringDecoder = stringDecoder;
		this.infoLevel = infoLevel;
		this.errorLevel = errorLevel;
	}

	@Override
	protected String getRewrittenDefiningClass(String definingClass) {
		return stringDecoder.decodeString(definingClass);
	}

	@Override
	public String rewriteItem(String definingClass, ItemType itemType, String value) {
		ErrorHandler errorHandler = new ErrorHandler(definingClass, itemType, value);
		String decodedValue = stringDecoder.decodeString(value, errorHandler);
		if (decodedValue != value && logger.isLogging(infoLevel) && !decodedValue.equals(value)) {
			StringBuilder sb = getMessageHeader(definingClass, itemType, value);
			sb.append("decoded to '").append(formatValue(itemType, decodedValue)).append("'");
			logger.log(infoLevel, sb.toString());
		}
		return decodedValue;
	}

}
