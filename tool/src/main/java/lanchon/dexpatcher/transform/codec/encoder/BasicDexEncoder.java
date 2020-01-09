/*
 * DexPatcher - Copyright 2015-2020 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.codec.encoder;

import lanchon.dexpatcher.core.logger.Logger;
import lanchon.dexpatcher.transform.TransformLogger;
import lanchon.dexpatcher.transform.codec.DexCodec;
import lanchon.dexpatcher.transform.codec.DexCodecModule.ItemType;

public final class BasicDexEncoder extends DexCodec {

	private final BasicStringEncoder basicStringEncoder;
	private final Logger.Level infoLevel;

	public BasicDexEncoder(BasicStringEncoder basicStringEncoder, TransformLogger logger, String logPrefix,
			Logger.Level infoLevel) {
		super(logger, logPrefix);
		this.basicStringEncoder = basicStringEncoder;
		this.infoLevel = infoLevel;
	}

	@Override
	protected String getTransformedDefiningClass(String definingClass) {
		return basicStringEncoder.encodeString(definingClass);
	}

	@Override
	public String rewriteItem(String definingClass, ItemType itemType, String value) {
		if (value == null) return null;
		String encodedValue = basicStringEncoder.encodeString(value);
		if (logger.isLogging(infoLevel) && !encodedValue.equals(value)) {
			StringBuilder sb = getMessageHeader(definingClass, itemType, value);
			sb.append("escaped to '").append(formatValue(itemType, encodedValue)).append("'");
			logger.log(infoLevel, sb.toString());
		}
		return encodedValue;
	}

}
