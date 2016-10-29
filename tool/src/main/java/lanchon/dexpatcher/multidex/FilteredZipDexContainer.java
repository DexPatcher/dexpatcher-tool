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
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.io.ByteStreamsHack;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.ZipDexContainer;
import org.jf.dexlib2.util.DexUtil;

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
	public FilteredZipDexFile getEntry(String entryName) throws IOException {
		if (!namer.isValidName(entryName)) return null;
		return (FilteredZipDexFile) super.getEntry(entryName);
	}

	@Override
	protected boolean isDex(ZipFile zipFile, ZipEntry zipEntry) throws IOException {
		if (!namer.isValidName(zipEntry.getName())) return false;
		return super.isDex(zipFile, zipEntry);
	}

	@Override
	protected FilteredZipDexFile loadEntry(ZipFile zipFile, ZipEntry zipEntry) throws IOException {
		InputStream inputStream = zipFile.getInputStream(zipEntry);
		try {
			byte[] buf = ByteStreamsHack.toByteArray(inputStream, zipEntry.getSize());
			Opcodes opcodes = super.getOpcodes();
			if (opcodes == null) {
				DexUtil.verifyDexHeader(buf, 0);
				opcodes = RawDexIO.getOpcodesFromDexHeader(buf, 0);
			}
			return new FilteredZipDexFile(opcodes, buf, zipEntry.getName());
		} finally {
			inputStream.close();
		}
	}

	public class FilteredZipDexFile extends ZipDexFile {

		protected FilteredZipDexFile(Opcodes opcodes, byte[] buf, String entryName) {
			super(opcodes, buf, entryName);
		}

		@Override
		public FilteredZipDexContainer getContainer() {
			return FilteredZipDexContainer.this;
		}

	}

}
