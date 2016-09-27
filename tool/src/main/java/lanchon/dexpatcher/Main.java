package lanchon.dexpatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
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

import static lanchon.dexpatcher.Logger.Level.*;

public class Main {

	public static void main(String[] args) {
		Locale locale = new Locale("en", "US");
		Locale.setDefault(locale);
		int value = new Main().run(args);
		System.exit(value);
	}

	public Logger logger;

	private String sourceFile;
	private List<String> patchFiles;
	private String patchedFile;
	private int apiLevel;
	private boolean experimental;

	public int run(String[] args) {
		logger = new BasicLogger(WARN);
		try {
			int value = parseCommandLine(args);
			if (value >= 0) return value;
			return processDexFiles();
		} catch (Exception e) {
			logger.log(FATAL, "exception: " + e);
			return 3;
		}
	}

	// Parse Command Line

	private int parseCommandLine(String[] args) {

		Options options = getOptions();
		try {

			CommandLine cl = new PosixParser().parse(options, args);

			if (cl.hasOption("help")) {
				printUsage(options);
				return 0;
			}

			if (cl.hasOption("version")) {
				System.out.println(getVersion());
				return 0;
			}

			if (cl.hasOption("quiet")) logger.setLogLevel(ERROR);
			if (cl.hasOption("verbose")) logger.setLogLevel(INFO);
			if (cl.hasOption("debug")) logger.setLogLevel(DEBUG);

			@SuppressWarnings("unchecked")
			List<String> files = cl.getArgList();
			if (files.isEmpty()) throw new ParseException("Missing argument: <source-dex-or-apk>");
			sourceFile = files.remove(0);
			patchFiles = files;
			patchedFile = cl.getOptionValue("output");

			Number apiNumber = (Number) cl.getParsedOptionValue("api-level");
			apiLevel = (apiNumber != null ? apiNumber.intValue() : 14);
			experimental = cl.hasOption("experimental");

			return -1;

		} catch (ParseException e) {
			logger.log(FATAL, e.getMessage());
			printUsage(options);
			return 1;
		}

	}

	private static Options getOptions() {
		Options options = new Options();
		Option o;
		o = new Option("o", "output", true, "name of patched dex file to write");
		o.setArgName("patched-dex"); options.addOption(o);
		o = new Option("a", "api-level", true, "api level of dex files (defaults to 14)");
		o.setArgName("n"); o.setType(Number.class); options.addOption(o);
		options.addOption(new Option("X", "experimental", false, "enable support for experimental opcodes"));
		options.addOption(new Option("q", "quiet", false, "do not output warnings"));
		options.addOption(new Option("v", "verbose", false, "output extra information"));
		options.addOption(new Option(null, "debug", false, "output debugging information"));
		options.addOption(new Option(null, "version", false, "print version information and exit"));
		options.addOption(new Option("?", "help", false, "print this help message and exit"));
		return options;
	}

	private static String getVersion() {
		String version = "<undefined>";
		final String file = "version";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream(file)));
			version = reader.readLine().trim();
			reader.close();
		} catch (Exception e) {}
		return version;
	}

	private static void printUsage(Options options) {
		System.out.println("DexPatcher Version " + getVersion() + " by Lanchon");
		String usage = "dexpatcher [<option> ...] [--output <patched-dex>] <source-dex-or-apk> [<patch-dex-or-apk> ...]";
		new HelpFormatter().printHelp(usage, options);
	}

	// Process Dex Files

	private int processDexFiles() throws IOException {

		DexFile dex = readDex(sourceFile);

		for (String patchFile : patchFiles) {
			DexFile patchDex = readDex(patchFile);
			dex = new DexPatcher(logger).process(dex, patchDex);
		}

		if (patchedFile == null) {
			logger.log(WARN, "dry run due to missing <patched-dex> output file argument");
		} else {
			if (logger.ok()) writeDex(patchedFile, dex);
		}

		logger.close();
		return logger.ok() ? 0 : 2;

	}

	private DexFile readDex(String name) throws IOException {
		logger.log(INFO, "read '" + name + "'");
		DexBackedDexFile dex = DexFileFactory.loadDexFile(name, apiLevel, experimental);
		if (dex.isOdexFile()) throw new RuntimeException(name + " is an odex file");
		return dex;
	}

	private void writeDex(String name, DexFile dex) throws IOException {
		logger.log(INFO, "write '" + name + "'");
		//DexFileFactory.writeDexFile(name, dex, apiLevel, experimental);
		writeDexWorkaround(name, dex, apiLevel, experimental);
	}

	private static void writeDexWorkaround(String path, DexFile input, int apiLevel, boolean experimental) throws IOException {
		// TODO: Remove this workaround when dexlib2 gets fixed.
		// See: https://github.com/JesusFreke/smali/issues/439
		DexPool dexPool = DexPool.makeDexPool(Opcodes.forApi(apiLevel, experimental));
		ClassSection classSection;
		try {
			Field classSectionField = DexWriter.class.getDeclaredField("classSection");
			classSectionField.setAccessible(true);
			classSection = (ClassSection) classSectionField.get(dexPool);
		} catch (ReflectiveOperationException e) {
			throw new Error(e);
		}
		ClassPool classPool = (ClassPool) classSection;
		for (ClassDef classDef: input.getClasses()) {
			classPool.intern(classDef);
		}
		dexPool.writeTo(new FileDataStore(new File(path)));
	}

}
