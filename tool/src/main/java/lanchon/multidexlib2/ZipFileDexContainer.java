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
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer.MultiDexFile;

public class ZipFileDexContainer extends AbstractMultiDexContainer<MultiDexFile> {

	public static boolean isZipFile(File zip) {
		if (!zip.isFile()) return false;
		try {
			ZipFile zipFile = new ZipFile(zip);
			zipFile.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public ZipFileDexContainer(File zip, DexFileNamer namer, Opcodes opcodes) throws IOException {
		Map<String, MultiDexFile> entryMap = new TreeMap<>(new DexFileNameComparator(namer));
		ZipFile zipFile = new ZipFile(zip);
		try {
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while (zipEntries.hasMoreElements()) {
				ZipEntry zipEntry = zipEntries.nextElement();
				String entryName = zipEntry.getName();
				if (namer.isValidName(entryName)) {
					DexFile dexFile;
					InputStream inputStream = zipFile.getInputStream(zipEntry);
					try {
						dexFile = RawDexIO.readRawDexFile(inputStream, zipEntry.getSize(), opcodes);
					} finally {
						inputStream.close();
					}
					MultiDexFile multiDexFile = new BasicMultiDexFile<>(this, entryName, dexFile);
					if (entryMap.put(entryName, multiDexFile) != null) throwDuplicateEntryName(entryName);
				}
			}
		} finally {
			zipFile.close();
		}
		initialize(entryMap, opcodes);
	}

}
