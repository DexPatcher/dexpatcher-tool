/*
 * DexPatcher - Copyright 2015, 2016 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.multidexlib2;

import java.util.Collections;
import java.util.Map;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer.MultiDexFile;

public class SingletonDexContainer extends AbstractMultiDexContainer<MultiDexFile> {

	// I insist that some dex container entries do not have names
	// even though dexlib2 does not allow null entry names.
	public static final String UNDEFINED_ENTRY_NAME = null;

	public SingletonDexContainer(DexFile dexFile) {
		this(UNDEFINED_ENTRY_NAME, dexFile);
	}

	public SingletonDexContainer(String entryName, DexFile dexFile) {
		Opcodes opcodes = dexFile.getOpcodes();
		MultiDexFile multiDexFile = new BasicMultiDexFile<>(this, entryName, dexFile);
		Map<String, MultiDexFile> entryMap = Collections.singletonMap(entryName, multiDexFile);
		initialize(entryMap, opcodes);
	}

}
