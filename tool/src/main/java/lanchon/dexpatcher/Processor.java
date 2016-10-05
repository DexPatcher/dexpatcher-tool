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

import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.core.DexPatcher;
import lanchon.dexpatcher.core.logger.Logger;
import lanchon.dexpatcher.multidex.BasicDexFileNamer;
import lanchon.dexpatcher.multidex.DexFileNamer;
import lanchon.dexpatcher.multidex.MultiDexIO;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.DexFile;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public class Processor {

	public static boolean processFiles(Logger logger, Configuration config) throws IOException {
		return new Processor(logger, config).processFiles();
	}

	private final Logger logger;
	private final Configuration config;

	private Opcodes opcodes;
	private DexFileNamer dexFileNamer;

	private Processor(Logger logger, Configuration config) {
		this.logger = logger;
		this.config = config;
	}

	private boolean processFiles() throws IOException {

		long time = System.nanoTime();

		logger.setLogLevel(config.logLevel);
		opcodes = Opcodes.forApi(config.apiLevel);
		dexFileNamer = new BasicDexFileNamer();

		DexFile dex = readDex(config.sourceFile);
		int types = dex.getClasses().size();

		for (String patchFile : config.patchFiles) {
			DexFile patchDex = readDex(patchFile);
			types += patchDex.getClasses().size();
			dex = processDex(dex, patchDex);
		}

		if (config.patchedFile == null) {
			logger.log(WARN, "dry run due to missing '--output' option");
		} else {
			if (logger.hasNotloggedErrors()) writeDex(config.patchedFile, dex);
		}

		time = System.nanoTime() - time;
		logStats("total process", types, time);

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
		logStats("patch process", sourceDex.getClasses().size() + patchDex.getClasses().size(), time);
		return patchedDex;
	}

	private DexFile readDex(String path) throws IOException {
		String message = "read '" + path + "'";
		logger.log(INFO, message);
		long time = System.nanoTime();
		DexFile dex = MultiDexIO.readMultiDexFile(config.multiDex, new File(path), dexFileNamer, opcodes);
		time = System.nanoTime() - time;
		logStats(message, dex.getClasses().size(), time);
		return dex;
	}

	private void writeDex(String path, DexFile dex) throws IOException {
		String message = "write '" + path + "'";
		logger.log(INFO, message);
		long time = System.nanoTime();
		DexFileFactory.writeDexFile(path, dex);
		time = System.nanoTime() - time;
		logStats(message, dex.getClasses().size(), time);
	}

	private void logStats(String header, int typeCount, long nanoTime) {
		if (config.timingStats) logger.log(NONE, "stats: " + header + ": " +
				typeCount + " types, " +
				((nanoTime + 500000) / 1000000) + " ms, " +
				(((nanoTime / typeCount) + 500) / 1000) + " us/type");

	}

}
