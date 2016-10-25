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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;

public class FilteredMultiDexContainer<T extends DexFile> implements MultiDexContainer<FilteredMultiDexContainer.FilteredMultiDexFile> {

	private final MultiDexContainer<T> container;
	private final DexFileNamer namer;
	private final boolean sort;

	public FilteredMultiDexContainer(MultiDexContainer<T> container, DexFileNamer namer, boolean sort) {
		this.container = container;
		this.namer = namer;
		this.sort = sort;
	}

	@Override
	public List<String> getDexEntryNames() throws IOException {
		List<String> filteredNames = new ArrayList<>();
		for (String name : container.getDexEntryNames()) {
			if (namer.isValidName(name)) filteredNames.add(name);
		}
		if (sort) {
			// TODO: Implement a numeric sort.
			Collections.sort(filteredNames);
		}
		return filteredNames;
	}

	@Override
	public FilteredMultiDexFile getEntry(String entryName) throws IOException {
		if (!namer.isValidName(entryName)) return null;
		return new FilteredMultiDexFile(container.getEntry(entryName), entryName);
	}

	@Override
	public Opcodes getOpcodes() {
		return container.getOpcodes();
	}

	public class FilteredMultiDexFile implements MultiDexContainer.MultiDexFile {

		private final DexFile dexFile;
		private final String entryName;

		private FilteredMultiDexFile(DexFile dexFile, String entryName) {
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
		public FilteredMultiDexContainer<T> getContainer() {
			return FilteredMultiDexContainer.this;
		}

	}

}
