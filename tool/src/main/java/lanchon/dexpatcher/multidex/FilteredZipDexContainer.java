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
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.ZipDexContainer;

public class FilteredZipDexContainer extends ZipDexContainer {

	private final DexFileNamer namer;
	private final boolean sort;

	public FilteredZipDexContainer(File zipFilePath, DexFileNamer namer, boolean sort, Opcodes opcodes) {
		super(zipFilePath, opcodes);
		this.namer = namer;
		this.sort = sort;
	}

	@Override
	public List<String> getDexEntryNames() throws IOException {
		List<String> filteredNames = super.getDexEntryNames();
		if (sort) {
			Collections.sort(filteredNames, new DexFileNamer.Comparator(namer));
		}
		return filteredNames;
	}

	@Override
	public ZipDexFile getEntry(String entryName) throws IOException {
		if (!namer.isValidName(entryName)) return null;
		return super.getEntry(entryName);
	}

	@Override
	protected boolean isDex(ZipFile zipFile, ZipEntry zipEntry) throws IOException {
		if (!namer.isValidName(zipEntry.getName())) return false;
		return super.isDex(zipFile, zipEntry);
	}

}
