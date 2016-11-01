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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.List;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;

public abstract class AbstractMultiDexContainer<T extends DexFile> implements MultiDexContainer<T> {

	private Map<String, T> entryMap;
	private List<String> entryNames;
	private Opcodes resolvedOpcodes;

	protected AbstractMultiDexContainer() {}

	protected void initialize(Map<String, T> entryMap, Opcodes opcodes) {
		if (entryMap == null) throw new NullPointerException("entryMap");
		if (this.entryMap != null) throw new IllegalStateException("Already initialized");
		this.entryMap = entryMap;
		// See: https://github.com/JesusFreke/smali/issues/458
		entryNames = Collections.unmodifiableList(new ArrayList<>(entryMap.keySet()));
		if (opcodes == null) {
			//opcodes = getNewestOpcodes();
			for (T entry : entryMap.values()) {
				opcodes = OpcodeUtils.getNewestOpcodesNullable(opcodes, entry.getOpcodes());
			}
			//if (opcodes == null) throw nullOpcodes();
		}
		resolvedOpcodes = opcodes;
	}

	@Override
	public List<String> getDexEntryNames() {
		return entryNames;
	}

	@Override
	public T getEntry(String entryName) {
		return entryMap.get(entryName);
	}

	@Override
	public Opcodes getOpcodes() {
		//if (resolvedOpcodes == null) throw nullOpcodes();
		return resolvedOpcodes;
	}

	/*
	public Opcodes getNewestOpcodes() {
		Opcodes opcodes = null;
		for (String entryName : getDexEntryNames()) {
			opcodes = OpcodeUtils.getNewestOpcodesNullable(opcodes, getEntry(entryName).getOpcodes());
		}
	}
	*/

	protected DuplicateEntryNameException throwDuplicateEntryName(String entryName) {
		throw new DuplicateEntryNameException(entryName);
	}

}
