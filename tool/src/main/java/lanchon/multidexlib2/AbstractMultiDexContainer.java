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

import java.io.IOException;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;

public abstract class AbstractMultiDexContainer<T extends DexFile> implements MultiDexContainer<T> {

	protected final Opcodes opcodes;

	public AbstractMultiDexContainer(Opcodes opcodes) {
		this.opcodes = opcodes;
	}

	@Override
	public Opcodes getOpcodes() {
		/*
		try {
			return getResolvedOpcodes();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		*/
		return opcodes;
	}

	public Opcodes getResolvedOpcodes() throws IOException {
		Opcodes value = opcodes;
		if (value == null) {
			for (String entryName : getDexEntryNames()) {
				value = OpcodeUtils.getNewerNullableOpcodes(value, getEntry(entryName).getOpcodes());
			}
		}
		return value;
	}

}
