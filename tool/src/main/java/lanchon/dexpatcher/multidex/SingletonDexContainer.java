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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Objects;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.jf.dexlib2.iface.MultiDexContainer.MultiDexFile;

public class SingletonDexContainer implements MultiDexContainer<MultiDexFile> {

	// I insist that some dex container entries do not have names
	// even though dexlib2 does not allow null entry names.
	public static final String UNDEFINED_ENTRY_NAME = null;

	private final List<String> entryNames;
	private final BasicMultiDexFile entry;

	public SingletonDexContainer(DexFile dexFile) {
		this(UNDEFINED_ENTRY_NAME, dexFile);
	}

	public SingletonDexContainer(String entryName, DexFile dexFile) {
		entryNames = Collections.singletonList(entryName);
		entry = new BasicMultiDexFile<>(this, entryName, dexFile);
	}

	@Override
	public List<String> getDexEntryNames() throws IOException {
		return entryNames;
	}

	@Override
	public MultiDexFile getEntry(String entryName) throws IOException {
		if (!Objects.equal(entryName, entry.getEntryName())) return null;
		return entry;
	}

	@Override
	public Opcodes getOpcodes() {
		return entry.getOpcodes();
	}

}
