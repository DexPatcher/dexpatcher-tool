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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.jf.dexlib2.iface.MultiDexContainer.MultiDexFile;

public class ZipFileDexContainer implements MultiDexContainer<MultiDexFile> {

	private final File zip;
	private final DexFileNamer namer;
	private final boolean sort;
	private final Opcodes opcodes;

	public ZipFileDexContainer(File zip, DexFileNamer namer, boolean sort, Opcodes opcodes) {
		this.zip = zip;
		this.namer = namer;
		this.sort = sort;
		this.opcodes = opcodes;
	}

	public boolean isZipFile() throws IOException {
		if (!zip.isFile()) return false;
		try {
			ZipFile zipFile = new ZipFile(zip);
			zipFile.close();
			return true;
		} catch (ZipException e) {
			return false;
		}
	}

	@Override
	public List<String> getDexEntryNames() throws IOException {
		List<String> filteredNames = new ArrayList<>();
		ZipFile zipFile = new ZipFile(zip);
		try {
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while (zipEntries.hasMoreElements()) {
				ZipEntry zipEntry = zipEntries.nextElement();
				String name = zipEntry.getName();
				if (namer.isValidName(name)) filteredNames.add(name);
			}
		} finally {
			zipFile.close();
		}
		if (sort) {
			Collections.sort(filteredNames, new DexFileNamer.Comparator(namer));
		}
		return filteredNames;
	}

	@Override
	public MultiDexFile getEntry(String entryName) throws IOException {
		if (!namer.isValidName(entryName)) return null;
		DexFile dexFile;
		ZipFile zipFile = new ZipFile(zip);
		try {
			ZipEntry zipEntry = zipFile.getEntry(entryName);
			if (zipEntry == null) return null;
			InputStream inputStream = zipFile.getInputStream(zipEntry);
			try {
				dexFile = RawDexIO.readRawDexFile(inputStream, zipEntry.getSize(), opcodes);
			} finally {
				inputStream.close();
			}
		} finally {
			zipFile.close();
		}
		return new BasicMultiDexFile<>(this, entryName, dexFile);
	}

	@Override
	public Opcodes getOpcodes() {
		return opcodes;
	}

}
