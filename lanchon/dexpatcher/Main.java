package lanchon.dexpatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.DexFile;

import static lanchon.dexpatcher.Logger.Level.*;

public class Main {

	public static void main(String[] args) {
		new Main(args);
	}

	private final Logger logger;

	public Main(String[] args) {

		Locale locale = new Locale("en", "US");
		Locale.setDefault(locale);

		Options options = getOptions();
		logger = new BasicLogger(WARN);
		try {

			CommandLine cl = new PosixParser().parse(options, args);

			if (cl.hasOption("help")) {
				printUsage(options);
				return;
			}

			if (cl.hasOption("version")) {
				System.out.println(getVersion());
				return;
			}

			if (cl.hasOption("quiet")) logger.setLogLevel(ERROR);
			if (cl.hasOption("verbose")) logger.setLogLevel(INFO);
			if (cl.hasOption("debug")) logger.setLogLevel(DEBUG);

			String patchedFile = cl.getOptionValue("output");
			Number apiNumber = (Number) cl.getParsedOptionValue("api-level");
			int api = (apiNumber != null ? apiNumber.intValue() : 14);

			@SuppressWarnings("unchecked")
			List<String> files = cl.getArgList();
			if (files.isEmpty()) throw new ParseException("Missing argument: <source-dex-or-apk>");
			String sourceFile = files.remove(0);

			DexFile dex = loadDex(sourceFile, api);
			for (String patchFile : files) {
				DexFile patchDex = loadDex(patchFile, api);
				dex = new DexPatcher(logger).process(dex, patchDex);
			}

			if (patchedFile == null) {
				logger.log(WARN, "dry run due to missing <patched-dex> output file argument");
			} else {
				if (logger.ok()) writeDex(patchedFile, dex);
			}
			logger.close();

		} catch (ParseException e) {
			logger.log(FATAL, e.getMessage());
			printUsage(options);
			return;
		} catch (Exception e) {
			logger.log(FATAL, "exception: " + e);
			return;
		} finally {
			if (!logger.ok()) System.exit(1);
		}

	}

	private DexFile loadDex(String name, int api) throws IOException {
		logger.log(INFO, "load '" + name + "'");
		DexBackedDexFile dex = DexFileFactory.loadDexFile(new File(name), api);
		if (dex.isOdexFile()) throw new RuntimeException(name + " is an odex file");
		return dex;
	}

	private void writeDex(String name, DexFile dex) throws IOException {
		logger.log(INFO, "write '" + name + "'");
		DexFileFactory.writeDexFile(name, dex);
	}

	private static Options getOptions() {
		Options options = new Options();
		Option o;
		o = new Option("a", "api-level", true,
				"api level of dex files (defaults to 14)\n" +
				"(needed for android 3 and earlier dex files)");
		o.setArgName("n"); o.setType(Number.class); options.addOption(o);
		o = new Option("o", "output", true, "name of patched dex file to write");
		o.setArgName("patched-dex"); options.addOption(o);
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

}
