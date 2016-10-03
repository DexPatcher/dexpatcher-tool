/*
 * DexPatcher - Copyright 2015, 2016 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.core.DexPatcher;
import lanchon.dexpatcher.core.logger.Logger;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.writer.ClassSection;
import org.jf.dexlib2.writer.DexWriter;
import org.jf.dexlib2.writer.io.FileDataStore;
import org.jf.dexlib2.writer.pool.ClassPool;
import org.jf.dexlib2.writer.pool.DexPool;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public class Processor {

	public static boolean processFiles(Logger logger, Configuration config) throws IOException {
		return new Processor(logger, config).processFiles();
	}

	private final Logger logger;
	private final Configuration config;

	private Opcodes opcodes;

	private Processor(Logger logger, Configuration config) {
		this.logger = logger;
		this.config = config;
	}

	private boolean processFiles() throws IOException {

		long time = System.nanoTime();

		logger.setLogLevel(config.logLevel);
		opcodes = Opcodes.forApi(config.apiLevel);

		DexFile dex = readDex(config.sourceFile);
		int types = dex.getClasses().size();

		for (String patchFile : config.patchFiles) {
			DexFile patchDex = readDex(patchFile);
			types += patchDex.getClasses().size();
			dex = processDex(dex, patchDex);
		}

		if (config.patchedFile == null) {
			logger.log(WARN, "dry run due to missing <patched-dex> output file argument");
		} else {
			if (logger.hasNotloggedErrors()) writeDex(config.patchedFile, dex);
		}

		time = System.nanoTime() - time;
		logStats("total stats", types, time);

		logger.logErrorAndWarningCounts();
		return logger.hasNotloggedErrors();

	}

	private Context createContext() {
		Context context = new Context(logger);
		context.setAnnotationPackage(config.annotationPackage);
		context.setDexTagSupported(config.dexTagSupported);
		String root = config.sourceCodeRoot;
		if (root != null && root.length() > 0 && !root.endsWith(File.separator)) root += File.separator;
		context.setSourceCodeRoot(root);
		return context;
	}

	private DexFile processDex(DexFile sourceDex, DexFile patchDex) {
		long time = System.nanoTime();
		DexFile patchedDex = DexPatcher.process(createContext(), sourceDex, patchDex);
		time = System.nanoTime() - time;
		logStats("process stats", sourceDex.getClasses().size() + patchDex.getClasses().size(), time);
		return patchedDex;
	}

	private DexFile readDex(String path) throws IOException {
		logger.log(INFO, "read '" + path + "'");
		long time = System.nanoTime();
		DexBackedDexFile dex = DexFileFactory.loadDexFile(new File(path), null, opcodes);
		if (dex.isOdexFile()) throw new RuntimeException(path + " is an odex file");
		time = System.nanoTime() - time;
		logStats("read stats", dex.getClassCount(), time);
		return dex;
	}

	private void writeDex(String path, DexFile dex) throws IOException {
		logger.log(INFO, "write '" + path + "'");
		long time = System.nanoTime();
		DexFileFactory.writeDexFile(path, dex);		// bug fixed in dexlib2-dexpatcher
		//writeDexFileWorkaround(path, dex);
		time = System.nanoTime() - time;
		logStats("write stats", dex.getClasses().size(), time);
	}

	private void logStats(String header, int typeCount, long nanoTime) {
		if (config.timingStats) logger.log(INFO, header + ": " +
				typeCount + " types, " +
				((nanoTime + 500000) / 1000000) + " ms, " +
				(((nanoTime / typeCount) + 500) / 1000) + " us/type");

	}

	private static void writeDexFileWorkaround(String path, DexFile dex) throws IOException {
		// DexFileFactory.writeDexFile() ignores the value of dex.getOpcodes().
		// For details, see: https://github.com/JesusFreke/smali/issues/439
		// TODO: Remove this workaround when dexlib2 gets fixed.
		DexPool dexPool = DexPool.makeDexPool(dex.getOpcodes());
		ClassSection classSection;
		try {
			Field classSectionField = DexWriter.class.getDeclaredField("classSection");
			classSectionField.setAccessible(true);
			classSection = (ClassSection) classSectionField.get(dexPool);
		} catch (ReflectiveOperationException e) {
			throw new Error(e);
		}
		ClassPool classPool = (ClassPool) classSection;
		for (ClassDef classDef : dex.getClasses()) {
			classPool.intern(classDef);
		}
		dexPool.writeTo(new FileDataStore(new File(path)));
	}

}
