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

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer.MultiDexFile;

public class DirectoryDexContainer extends AbstractMultiDexContainer<MultiDexFile> {

	public DirectoryDexContainer(File directory, DexFileNamer namer, Opcodes opcodes) throws IOException {
		Map<String, MultiDexFile> entryMap = new TreeMap<>(new DexFileNameComparator(namer));
		String[] names = directory.list();
		if (names == null) throw new IOException("Cannot access directory: " + directory);
		for (String entryName : names) {
			File file = new File(directory, entryName);
			if (file.isFile() && namer.isValidName(entryName)) {
				DexFile dexFile = RawDexIO.readRawDexFile(file, opcodes);
				MultiDexFile multiDexFile = new BasicMultiDexFile<>(this, entryName, dexFile);
				if (entryMap.put(entryName, multiDexFile) != null) throwDuplicateEntryName(entryName);
			}
		}
		initialize(entryMap, opcodes);
	}

}
