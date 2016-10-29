/*
 * DexPatcher - Copyright 2015, 2016 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.multidex;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.VersionMap;

public class OpcodeUtils {

	public static Opcodes getOpcodesFromDexVersion(int dexVersion) {
		return Opcodes.forApi(DexVersionMap.getHighestApiLevelFromDexVersion(dexVersion));
	}

	public static Opcodes getNewerOpcodes(Opcodes o1, Opcodes o2) {
		if (o1.api == VersionMap.NO_VERSION || o2.api == VersionMap.NO_VERSION) {
			throw new IllegalArgumentException("Opcodes instance has undefined api level");
		}
		return o1.api >= o2.api ? o1 : o2;
	}

	private OpcodeUtils() {}

}
