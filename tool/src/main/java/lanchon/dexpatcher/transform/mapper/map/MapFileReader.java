/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.mapper.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lanchon.dexpatcher.core.logger.Logger;
import lanchon.dexpatcher.transform.mapper.map.builder.MapBuilder;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public final class MapFileReader {

	public static void read(File file, boolean fileNameOnly, MapBuilder mapBuilder, Logger logger) throws IOException {
		String fileName = fileNameOnly ? file.getName() : file.getPath();
		read(file, fileName, mapBuilder, logger);
	}

	public static void read(File file, String fileName, MapBuilder mapBuilder, Logger logger) throws IOException {
		try (InputStream inputStream = new FileInputStream(file)) {
			Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
			read(reader, fileName, mapBuilder, logger);
		}
	}

	public static void read(Reader reader, String fileName, MapBuilder mapBuilder, Logger logger) throws IOException {
		read(new LineNumberReader(reader), fileName, mapBuilder, logger);
	}

	public static void read(LineNumberReader reader, String fileName, MapBuilder mapBuilder, Logger logger)
			throws IOException {
		new MapFileReader(reader, fileName, mapBuilder, logger).read();
	}

	private static final String PATTERN_PART_MAPPING = "\\s*->\\s*(?<new>\\S+)\\s*";

	private static final Pattern PATTERN_EMPTY_LINE = Pattern.compile(
			"\\s*(?:#.*)?"
	);

	private static final Pattern PATTERN_TYPE = Pattern.compile(
			"\\s*(?<old>\\S+)" +
			"\\s*(?:->\\s*(?<new>\\S+?)\\s*(?::\\s*)?)|(?::\\s*)"
	);

	private static final Pattern PATTERN_FIELD = Pattern.compile(
			"\\s*(?<type>\\S+)" +
			"\\s+(?<old>(?:\\S*[\\S&&[^)]])|(?:[\\S&&[^(]]*[)]))" +
			PATTERN_PART_MAPPING
	);

	private static final Pattern PATTERN_METHOD = Pattern.compile(
			"\\s*(?<ret>\\S+)" +
			"\\s+(?<old>\\S+)" +
			"\\s*[(]\\s*(?<args>(?:[\\S&&[^(),]]+(?:\\s*,\\s*[\\S&&[^(),]]+)*)?)\\s*[)]" +
			PATTERN_PART_MAPPING
	);

	private static final Pattern PATTERN_METHOD_PARAMETER_SEPARATOR = Pattern.compile(
			"\\s*,\\s*"
	);

	private final LineNumberReader reader;
	private final String fileName;
	private final MapBuilder mapBuilder;
	private final Logger logger;

	private String line;
	private MapBuilder.MemberMapBuilder memberMapBuilder;

	public MapFileReader(LineNumberReader reader, String fileName, MapBuilder mapBuilder, Logger logger) {
		this.reader = reader;
		this.fileName = fileName;
		this.mapBuilder = mapBuilder;
		this.logger = logger;
	}

	private void read() throws IOException {
		memberMapBuilder = null;
		while ((line = reader.readLine()) != null) {
			parseLine();
		}
	}

	private void parseLine() {
		int comment = line.indexOf(';');
		String l = (comment < 0) ? line : line.substring(0, comment);
		if (PATTERN_EMPTY_LINE.matcher(l).matches()) return;
		Matcher type = PATTERN_TYPE.matcher(l);
		if (type.matches()) {
			String name = type.group("old");
			String newName = type.group("new");
			if (newName == null) newName = name;
			parseType(name, newName);
			return;
		}
		Matcher field = PATTERN_FIELD.matcher(l);
		if (field.matches()) {
			parseField(field.group("type"), field.group("old"), field.group("new"));
			return;
		}
		Matcher method = PATTERN_METHOD.matcher(l);
		if (method.matches()) {
			String[] args = PATTERN_METHOD_PARAMETER_SEPARATOR.split(method.group("args"));
			if (args.length == 1 && args[0].isEmpty()) args = new String[] {};
			parseMethod(args, method.group("ret"), method.group("old"), method.group("new"));
			return;
		}
		log(ERROR, "syntax error");
		memberMapBuilder = null;
	}

	private void parseType(String name, String newName) {
		try {
			memberMapBuilder = null;
			memberMapBuilder = mapBuilder.addClassMapping(name, newName);
		} catch (MapBuilder.BuilderException e) {
			logBuilderException(e);
		}
	}

	private void parseField(String type, String name, String newName) {
		if (memberMapBuilder != null) {
			try {
				memberMapBuilder.addFieldMapping(type, name, newName);
			} catch (MapBuilder.BuilderException e) {
				logBuilderException(e);
			}
		} else {
			log(ERROR, "unexpected field mapping");
		}
	}

	private void parseMethod(String[] parameterTypes, String returnType, String name, String newName) {
		if (memberMapBuilder != null) {
			try {
				memberMapBuilder.addMethodMapping(parameterTypes, returnType, name, newName);
			} catch (MapBuilder.BuilderException e) {
				logBuilderException(e);
			}
		} else {
			log(ERROR, "unexpected method mapping");
		}
	}

	private void logBuilderException(MapBuilder.BuilderException exception) {
		log(ERROR, exception.getMessage());
	}

	private void log(Logger.Level level, String message) {
		String file = fileName != null ? ('(' + fileName + ':' + reader.getLineNumber() + "): ") : "";
		logger.log(level, "map file: " + file + message + ": " + line.trim());
		//memberMapBuilder = null;
	}

}
