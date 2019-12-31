/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.codec;

import lanchon.dexpatcher.transform.MemberLogger;
import lanchon.dexpatcher.transform.TransformLogger;
import lanchon.dexpatcher.transform.codec.DexCodecModule.ItemType;

import org.jf.dexlib2.rewriter.RewriterModule;

public abstract class DexCodec extends MemberLogger implements DexCodecModule.ItemRewriter {

	public static String formatValue(ItemType itemType, String value) {
		return itemType == ItemType.BINARY_CLASS_NAME ? value.replace('/', '.') : value;
	}

	public DexCodec(TransformLogger logger, String logPrefix) {
		super(logger, logPrefix);
	}

	public final RewriterModule getModule() {
		return new DexCodecModule(this);
	}

	public final StringBuilder getMessageHeader(String definingClass, ItemType itemType, String value) {
		StringBuilder sb = (definingClass != null) ? getMessageHeaderForMember(definingClass) : getMessageHeader();
		sb.append(itemType.label).append(" '").append(formatValue(itemType, value)).append("': ");
		return sb;
	}

}
