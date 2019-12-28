/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lanchon.dexpatcher.Processor.PreTransform;
import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.transform.anonymizer.TypeAnonymizer;
import lanchon.dexpatcher.transform.codec.StringCodec;
import lanchon.multidexlib2.DexIO;
import lanchon.multidexlib2.MultiDexIO;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public class Parser {

	public static Configuration parseCommandLine(String[] args) throws ParseException {

		Options options = getOptions();
		CommandLine cl = new DefaultParser().parse(options, args);

		if (cl.hasOption("help")) {
			printUsage();
			return null;
		}

		if (cl.hasOption("version")) {
			System.out.println(Main.getVersion());
			return null;
		}

		return parseCommandLine(cl);

	}

	public static Configuration parseCommandLine(CommandLine cl) throws ParseException {

		Configuration config = new Configuration();

		List<String> files = cl.getArgList();
		if (files.isEmpty()) {
			throw new ParseException("Missing argument: <source-dex-apk-or-dir>");
		}

		config.sourceFile = files.get(0);
		config.patchFiles = new ArrayList<>(files.subList(1, files.size()));

		parseMainOptions(cl, config);
		parseTransformOptions(cl, config);

		return config;

	}

	private static void parseMainOptions(CommandLine cl, Configuration config) throws ParseException {

		Number apiLevel = (Number) cl.getParsedOptionValue("api-level");
		if (apiLevel != null) config.apiLevel = apiLevel.intValue();

		config.multiDex = cl.hasOption("multi-dex");
		if (cl.hasOption("multi-dex-threaded")) { config.multiDex = true; config.multiDexJobs = 0; }
		Number multiDexJobs = (Number) cl.getParsedOptionValue("multi-dex-jobs");
		if (multiDexJobs != null) { config.multiDex = true; config.multiDexJobs = multiDexJobs.intValue(); }

		Number maxDexPoolSize = (Number) cl.getParsedOptionValue("max-dex-pool-size");
		if (maxDexPoolSize != null) config.maxDexPoolSize = maxDexPoolSize.intValue();

		config.annotationPackage = cl.getOptionValue("annotations", Context.DEFAULT_ANNOTATION_PACKAGE);
		if (config.annotationPackage.isEmpty()) config.annotationPackage = null;
		config.constructorAutoIgnoreDisabled = cl.hasOption("no-auto-ignore");

		config.patchedFile = cl.getOptionValue("output");
		config.dryRun = cl.hasOption("dry-run");

		config.logLevel = WARN;
		if (cl.hasOption("quiet")) config.logLevel = ERROR;
		if (cl.hasOption("verbose")) config.logLevel = INFO;
		if (cl.hasOption("debug")) config.logLevel = DEBUG;

		if (cl.hasOption("path")) config.sourceCodeRoot = "";
		config.sourceCodeRoot = cl.getOptionValue("path-root", config.sourceCodeRoot);
		config.timingStats = cl.hasOption("stats");

	}

	private static void parseTransformOptions(CommandLine cl, Configuration config) throws ParseException {

		config.mapSource = cl.hasOption("map-source");

		config.unmapSource = cl.hasOption("unmap-source");
		config.unmapPatches = cl.hasOption("unmap-patches");
		config.unmapOutput = cl.hasOption("unmap-output");

		String[] mapFiles = cl.getOptionValues("map");
		if (mapFiles != null) config.mapFiles = Arrays.asList(mapFiles);
		if (config.mapFiles == null && (config.mapSource || config.unmapSource || config.unmapPatches || config.unmapOutput)) {
			throw new ParseException("Missing option: map");
		}
		config.invertMap = cl.hasOption("invert-map");

		config.deanonSource = cl.hasOption("deanon-source");
		config.deanonSourceAlternate = cl.hasOption("deanon-source-alt");
		if (config.deanonSource && config.deanonSourceAlternate) {
			throw new ParseException("Incompatible options: deanon-source, deanon-source-alt");
		}
		config.deanonPatches = cl.hasOption("deanon-patches");
		config.deanonPatchesAlternate = cl.hasOption("deanon-patches-alt");
		if (config.deanonPatches && config.deanonPatchesAlternate) {
			throw new ParseException("Incompatible options: deanon-patches, deanon-patches-alt");
		}

		config.reanonSource = cl.hasOption("reanon-source");
		config.reanonPatches = cl.hasOption("reanon-patches");
		config.reanonOutput = cl.hasOption("reanon-output");

		config.mainAnonymizationPlan = cl.getOptionValue("main-plan", TypeAnonymizer.DEFAULT_MAIN_ANONYMIZATION_PLAN);
		if (!TypeAnonymizer.isValidPlan(config.mainAnonymizationPlan)) {
			throw new ParseException("Invalid main anonymization plan: '" + config.mainAnonymizationPlan + "'");
		}
		config.alternateAnonymizationPlan = cl.getOptionValue("alt-plan", TypeAnonymizer.DEFAULT_ALTERNATE_ANONYMIZATION_PLAN);
		if (!TypeAnonymizer.isValidPlan(config.alternateAnonymizationPlan)) {
			throw new ParseException("Invalid alternate anonymization plan: '" + config.alternateAnonymizationPlan + "'");
		}
		config.treatReanonymizeErrorsAsWarnings = cl.hasOption("no-reanon-errors");

		config.decodeSource = cl.hasOption("decode-source");
		config.decodePatches = cl.hasOption("decode-patches");
		config.decodeOutput = cl.hasOption("decode-output");

		config.codeMarker = cl.getOptionValue("code-marker", StringCodec.DEFAULT_CODE_MARKER);
		if (!StringCodec.isValidCodeMarker(config.codeMarker)) {
			throw new ParseException("Invalid code marker: '" + config.codeMarker + "'");
		}
		config.treatDecodeErrorsAsWarnings = cl.hasOption("no-decode-errors");

		String preTransformSet = cl.getOptionValue("pre-transform", null);
		if (preTransformSet != null) {
			config.preTransform = PreTransform.parse(preTransformSet);
			if (config.preTransform == null) {
				throw new ParseException("Invalid pre-transform set: '" + preTransformSet + "'");
			}
		}

	}

	public static void printUsage() {
		printUsage(System.out);
	}

	public static void printUsage(PrintStream out) {
		PrintWriter writer = new PrintWriter(out);
		HelpFormatter formatter = new HelpFormatter();
		formatter.setOptionComparator(null);
		printUsage(writer, formatter);
		writer.flush();
	}

	public static void printUsage(PrintWriter writer, HelpFormatter formatter) {
		int width = formatter.getWidth();
		int leftPadding = formatter.getLeftPadding();
		int descPadding = formatter.getDescPadding();
		String usage = "dexpatcher [<option> ...] [--output <patched-dex-or-dir>] " +
				"<source-dex-apk-or-dir> [<patch-dex-apk-or-dir> ...]";
		formatter.printWrapped(writer, width, Main.getHeader());
		writer.println();
		formatter.printUsage(writer, width, usage);
		writer.println();
		formatter.printWrapped(writer, width, "main options:");
		formatter.printOptions(writer, width, addMainOptions(new Options()), leftPadding, descPadding);
		writer.println();
		formatter.printWrapped(writer, width, "code transform options:");
		formatter.printOptions(writer, width, addTransformOptions(new Options()), leftPadding, descPadding);
		writer.println();
	}

	private static Options getOptions() {
		Options options = new Options();
		addMainOptions(options);
		addTransformOptions(options);
		return options;
	}

	private static Options addMainOptions(Options options) {

		options.addOption(Option.builder("a").longOpt("api-level").hasArg().argName("n").type(Number.class).desc("android api level (default: auto-detect)").build());

		options.addOption(Option.builder("m").longOpt("multi-dex").desc("enable multi-dex support").build());
		options.addOption(Option.builder("M").longOpt("multi-dex-threaded").desc("multi-threaded multi-dex (implies: -m)").build());
		options.addOption(Option.builder("J").longOpt("multi-dex-jobs").hasArg().argName("n").type(Number.class).desc("multi-dex thread count (implies: -m -M) (default: " + "available processors up to " + MultiDexIO.DEFAULT_MAX_THREADS + ")").build());

		options.addOption(Option.builder().longOpt("max-dex-pool-size").hasArg().argName("n").type(Number.class).desc("maximum size of dex pools (default: " + DexIO.DEFAULT_MAX_DEX_POOL_SIZE + ")").build());

		options.addOption(Option.builder().longOpt("annotations").hasArg().argName("package").desc("package name of DexPatcher annotations (default: '" + Context.DEFAULT_ANNOTATION_PACKAGE + "')").build());
		options.addOption(Option.builder().longOpt("no-auto-ignore").desc("no trivial default constructor auto-ignore").build());

		options.addOption(Option.builder("o").longOpt("output").hasArg().argName("dex-or-dir").desc("name of output file or directory").build());
		options.addOption(Option.builder().longOpt("dry-run").desc("do not write output files (much faster)").build());

		options.addOption(Option.builder("q").longOpt("quiet").desc("do not output warnings").build());
		options.addOption(Option.builder("v").longOpt("verbose").desc("output extra information").build());
		options.addOption(Option.builder("d").longOpt("debug").desc("output debugging information").build());

		options.addOption(Option.builder("p").longOpt("path").desc("output relative paths of source code files").build());
		options.addOption(Option.builder("P").longOpt("path-root").hasArg().argName("root").desc("output absolute paths of source code files").build());
		options.addOption(Option.builder().longOpt("stats").desc("output timing statistics").build());

		options.addOption(Option.builder("h").longOpt("help").desc("print this help message and exit").build());
		options.addOption(Option.builder().longOpt("version").desc("print version information and exit").build());

		return options;

	}

	private static Options addTransformOptions(Options options) {

		options.addOption(Option.builder().longOpt("map-source").desc("apply map to identifiers in source").build());

		options.addOption(Option.builder().longOpt("unmap-source").desc("apply map inverse to identifiers in source").build());
		options.addOption(Option.builder().longOpt("unmap-patches").desc("apply map inverse to identifiers in patches").build());
		options.addOption(Option.builder().longOpt("unmap-output").desc("apply map inverse to identifiers in output").build());

		options.addOption(Option.builder().longOpt("map").hasArgs().argName("file").desc("identifier map file (repeatable option)").build());
		options.addOption(Option.builder().longOpt("invert-map").desc("use inverse of identifier map file").build());

		options.addOption(Option.builder().longOpt("deanon-source").desc("deanonymize anonymous classes in source").build());
		options.addOption(Option.builder().longOpt("deanon-source-alt").desc("deanonymize source with alternate plan").build());
		options.addOption(Option.builder().longOpt("deanon-patches").desc("deanonymize anonymous classes in patches").build());
		options.addOption(Option.builder().longOpt("deanon-patches-alt").desc("deanonymize patches with alternate plan").build());
		options.addOption(Option.builder().longOpt("reanon-source").desc("reanonymize anonymous classes in source").build());
		options.addOption(Option.builder().longOpt("reanon-patches").desc("reanonymize anonymous classes in patches").build());
		options.addOption(Option.builder().longOpt("reanon-output").desc("reanonymize anonymous classes in output").build());

		options.addOption(Option.builder().longOpt("main-plan").hasArg().argName("anon-plan").desc("main anonymization plan (default: '" + TypeAnonymizer.DEFAULT_MAIN_ANONYMIZATION_PLAN + "')").build());
		options.addOption(Option.builder().longOpt("alt-plan").hasArg().argName("anon-plan").desc("alternate plan (default: '" + TypeAnonymizer.DEFAULT_ALTERNATE_ANONYMIZATION_PLAN + "')").build());
		options.addOption(Option.builder().longOpt("no-reanon-errors").desc("treat reanonymize errors as warnings").build());

		options.addOption(Option.builder().longOpt("decode-source").desc("decode identifiers in source").build());
		options.addOption(Option.builder().longOpt("decode-patches").desc("decode identifiers in patches").build());
		options.addOption(Option.builder().longOpt("decode-output").desc("decode identifiers in output").build());

		options.addOption(Option.builder().longOpt("code-marker").hasArg().argName("marker").desc("identifier code marker (default: '" + StringCodec.DEFAULT_CODE_MARKER + "')").build());
		options.addOption(Option.builder().longOpt("no-decode-errors").desc("treat decode errors as warnings").build());

		StringBuilder preTransformSets = new StringBuilder();
		for (PreTransform pt : PreTransform.values()) {
			if (preTransformSets.length() != 0) preTransformSets.append("|");
			preTransformSets.append("'").append(pt.format()).append("'");
		}
		options.addOption(Option.builder().longOpt("pre-transform").hasArg().argName("set").desc("add pre-transform stages (default: '" + Processor.DEFAULT_PRE_TRANSFORM.format() + "') (<set>: " + preTransformSets + ")").build());

		return options;

	}

	private Parser() {}

}
