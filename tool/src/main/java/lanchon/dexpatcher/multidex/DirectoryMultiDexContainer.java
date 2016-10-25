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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;

public class DirectoryMultiDexContainer implements MultiDexContainer<DirectoryMultiDexContainer.DirectoryMultiDexFile> {

	private final File directory;
	private final DexFileNamer namer;               // TODO: Remove when dexEntryNames becomes a Set.
	private final Opcodes opcodes;
	private final List<String> dexEntryNames;       // TODO: Should change to Set<String> upstream.

	public DirectoryMultiDexContainer(File directory, DexFileNamer namer, Opcodes opcodes) throws IOException {
		this.directory = directory;
		this.namer = namer;
		this.opcodes = opcodes;
		String[] names = directory.list();
		if (names == null) throw new IOException("Cannot access directory: " + directory);
		// TODO: Implement a numeric sort.
		Arrays.sort(names);
		List<String> filteredNames = new ArrayList<>();
		for (String name : names) {
			if (new File(directory, name).isFile() && namer.isValidName(name)) {
				filteredNames.add(name);
			}
		}
		dexEntryNames = Collections.unmodifiableList(filteredNames);
	}

	@Override
	public List<String> getDexEntryNames() {
		return dexEntryNames;
	}

	@Override
	public DirectoryMultiDexFile getEntry(String entryName) throws IOException {
		// TODO: Change when dexEntryNames becomes a Set.
		//if (!dexEntryNames.contains(entryName)) return null;
		if (!(new File(directory, entryName).isFile() && namer.isValidName(entryName))) return null;
		File file = new File(directory, entryName);
		DexFile dexFile = MultiDexIO.readRawDexFile(file, opcodes);
		return new DirectoryMultiDexFile(dexFile, entryName);
	}

	@Override
	public Opcodes getOpcodes() {
		return opcodes;
	}

	public class DirectoryMultiDexFile implements MultiDexContainer.MultiDexFile {

		private final DexFile dexFile;
		private final String entryName;

		private DirectoryMultiDexFile(DexFile dexFile, String entryName) {
			this.dexFile = dexFile;
			this.entryName = entryName;
		}

		@Override
		public Set<? extends ClassDef> getClasses() {
			return dexFile.getClasses();
		}

		@Override
		public Opcodes getOpcodes() {
			return dexFile.getOpcodes();
		}

		@Override
		public String getEntryName() {
			return entryName;
		}

		@Override
		public DirectoryMultiDexContainer getContainer() {
			return DirectoryMultiDexContainer.this;
		}

	}

}
