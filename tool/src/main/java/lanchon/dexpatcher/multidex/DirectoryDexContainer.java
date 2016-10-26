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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.jf.dexlib2.iface.MultiDexContainer.MultiDexFile;

public class DirectoryDexContainer implements MultiDexContainer<MultiDexFile> {

	private final File directory;
	private final DexFileNamer namer;
	private final Opcodes opcodes;

	public DirectoryDexContainer(File directory, DexFileNamer namer, Opcodes opcodes) {
		this.directory = directory;
		this.namer = namer;
		this.opcodes = opcodes;
	}

	@Override
	public List<String> getDexEntryNames() throws IOException {
		String[] names = directory.list();
		if (names == null) throw new IOException("Cannot access directory: " + directory);
		List<String> filteredNames = new ArrayList<>();
		for (String name : names) {
			if (new File(directory, name).isFile() && namer.isValidName(name)) filteredNames.add(name);
		}
		Collections.sort(filteredNames, new DexFileNamer.Comparator(namer));
		return filteredNames;
	}

	@Override
	public MultiDexFile getEntry(String entryName) throws IOException {
		if (!(new File(directory, entryName).isFile() && namer.isValidName(entryName))) return null;
		File file = new File(directory, entryName);
		DexFile dexFile = RawDexIO.readRawDexFile(file, opcodes);
		return new BasicMultiDexFile<>(this, entryName, dexFile);
	}

	@Override
	public Opcodes getOpcodes() {
		return opcodes;
	}

}
