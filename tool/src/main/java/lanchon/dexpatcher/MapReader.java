/*
 * DexPatcher - Copyright 2015-2020 Rodrigo Balerdi
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

import lanchon.dexpatcher.core.logger.Logger;
import lanchon.dexpatcher.transform.mapper.MapFileReader;
import lanchon.dexpatcher.transform.mapper.map.DexMap;
import lanchon.dexpatcher.transform.mapper.map.builder.CompositeMapBuilder;
import lanchon.dexpatcher.transform.mapper.map.builder.DexMapping;
import lanchon.dexpatcher.transform.mapper.map.builder.InverseMapBuilder;
import lanchon.dexpatcher.transform.mapper.map.builder.MapBuilder;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public final class MapReader {

	public static boolean readMapPair(Iterable<String> mapFiles, DexMap inverseComposeMap, boolean invertMap,
			DexMapping directMap, DexMapping inverseMap, Logger logger) throws IOException {
		return readMapPair(mapFiles, inverseComposeMap, invertMap ? inverseMap : directMap, invertMap ? directMap : inverseMap,
				logger);
	}

	public static boolean readMapPair(Iterable<String> mapFiles, DexMap inverseComposeMap, DexMapping directMap,
			MapBuilder inverseMap, Logger logger) throws IOException {
		// The direct map is needed to read the inverse map. (It will be discarded if not needed further.)
		if (directMap == null) {
			if (inverseMap == null) return true;
			directMap = new DexMapping();
		}
		MapBuilder directMapBuilder = CompositeMapBuilder.of(directMap, inverseComposeMap);
		boolean success = readMap(mapFiles, directMapBuilder, logger);
		// Read the inverse map only if needed. (It is more memory efficient to read it again from disk.)
		if (inverseMap != null && (success || !Processor.ABORT_ON_EARLY_ERRORS)) {
			MapBuilder inverseMapBuilder = new InverseMapBuilder(inverseMap, directMap);
			inverseMapBuilder = CompositeMapBuilder.of(inverseMapBuilder, inverseComposeMap);
			success = readMap(mapFiles, inverseMapBuilder, logger) && success;
		}
		return success;
	}

	public static boolean readMap(Iterable<String> mapFiles, MapBuilder mapBuilder, Logger logger) throws IOException {
		int errors = logger.getMessageCount(FATAL) + logger.getMessageCount(ERROR);
		for (String mapFile : mapFiles) {
			MapFileReader.read(new File(mapFile), true, mapBuilder, logger);
		}
		return (errors == (logger.getMessageCount(FATAL) + logger.getMessageCount(ERROR)));
	}

	private MapReader() {}

}
