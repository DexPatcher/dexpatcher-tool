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

import java.util.List;

import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.multidex.MultiDexIO;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public class Parser {

	public static final int DEFAULT_API_LEVEL_UNI_DEX = 14;
	public static final int DEFAULT_API_LEVEL_MULTI_DEX = 21;

	public static Configuration parseCommandLine(String[] args) throws ParseException {

		Configuration config = new Configuration();

		Options options = getOptions();
		CommandLine cl = new PosixParser().parse(options, args);

		if (cl.hasOption("help")) {
			printUsage();
			return null;
		}

		if (cl.hasOption("version")) {
			System.out.println(Main.getVersion());
			return null;
		}

		@SuppressWarnings("unchecked")
		List<String> files = cl.getArgList();
		if (files.isEmpty()) throw new ParseException("Missing argument: <source-dex-apk-or-dir>");
		config.sourceFile = files.remove(0);
		config.patchFiles = files;
		config.patchedFile = cl.getOptionValue("output");

		config.multiDex = cl.hasOption("multi-dex");
		if (cl.hasOption("multi-dex-mt")) { config.multiDex = true; config.multiDexJobs = 0; }
		Number multiDexJobs = (Number) cl.getParsedOptionValue("multi-dex-jobs");
		if (multiDexJobs != null) { config.multiDex = true; config.multiDexJobs = multiDexJobs.intValue(); }

		Number apiLevel = (Number) cl.getParsedOptionValue("api-level");
		config.apiLevel = (apiLevel != null ? apiLevel.intValue() :
				(config.multiDex ? DEFAULT_API_LEVEL_MULTI_DEX : DEFAULT_API_LEVEL_UNI_DEX ));

		config.annotationPackage = cl.getOptionValue("annotations", Context.DEFAULT_ANNOTATION_PACKAGE);
		config.dexTagSupported = cl.hasOption("compat-dextag");

		config.logLevel = WARN;
		if (cl.hasOption("quiet")) config.logLevel = ERROR;
		if (cl.hasOption("verbose")) config.logLevel = INFO;
		if (cl.hasOption("debug")) config.logLevel = DEBUG;

		if (cl.hasOption("path")) config.sourceCodeRoot = "";
		config.sourceCodeRoot = cl.getOptionValue("path-root", config.sourceCodeRoot);
		config.timingStats = cl.hasOption("stats");

		return config;

	}

	public static void printUsage() {
		System.out.println("DexPatcher Version " + Main.getVersion() + " by Lanchon");
		System.out.println("           https://dexpatcher.github.io/");
		String usage = "dexpatcher [<option> ...] [--output <patched-dex-or-dir>] " +
				"<source-dex-apk-or-dir> [<patch-dex-apk-or-dir> ...]";
		new HelpFormatter().printHelp(usage, getOptions());
	}

	private static Options getOptions() {

		Options options = new Options();
		Option o;

		o = new Option("o", "output", true, "name of output file or directory");
		o.setArgName("dex-or-dir"); options.addOption(o);

		o = new Option("a", "api-level", true, "android api level of dex files (default:" + System.lineSeparator() +
				DEFAULT_API_LEVEL_MULTI_DEX + " if multi-dex is enabled, " + DEFAULT_API_LEVEL_UNI_DEX + " otherwise)");
		o.setArgName("n"); o.setType(Number.class); options.addOption(o);

		options.addOption(new Option("m", "multi-dex", false, "enable multi-dex support"));
		options.addOption(new Option("M", "multi-dex-threaded", false, "multi-threaded multi-dex (implies: -m)"));
		o = new Option("J", "multi-dex-jobs", true, "multi-dex thread count (implies: -m -M) (default: available processors up to " +
				MultiDexIO.DEFAULT_MAX_THREADS + ")");
		o.setArgName("n"); o.setType(Number.class); options.addOption(o);

		o = new Option(null, "annotations", true, "package name of DexPatcher annotations (default: '" +
				Context.DEFAULT_ANNOTATION_PACKAGE + "')");
		o.setArgName("package"); options.addOption(o);
		options.addOption(new Option(null, "compat-dextag", false, "enable support for the deprecated DexTag"));

		options.addOption(new Option("q", "quiet", false, "do not output warnings"));
		options.addOption(new Option("v", "verbose", false, "output extra information"));
		options.addOption(new Option(null, "debug", false, "output debugging information"));

		options.addOption(new Option("p", "path", false, "output relative paths of source code files"));
		o = new Option(null, "path-root", true, "output absolute paths of source code files");
		o.setArgName("root"); options.addOption(o);
		options.addOption(new Option(null, "stats", false, "output timing statistics"));

		options.addOption(new Option(null, "version", false, "print version information and exit"));
		options.addOption(new Option("?", "help", false, "print this help message and exit"));

		return options;

	}

}
