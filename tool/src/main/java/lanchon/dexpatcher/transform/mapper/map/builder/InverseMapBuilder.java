/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.mapper.map.builder;

import lanchon.dexpatcher.transform.mapper.map.DexMap;
import lanchon.dexpatcher.transform.mapper.map.DexMaps;

public class InverseMapBuilder implements MapBuilder {

	private static final String EXCEPTION_HEADER = "on inverse map: ";

	protected final MapBuilder wrappedMapBuilder;
	protected final DexMap directDexMap;

	public InverseMapBuilder(MapBuilder wrappedMapBuilder, DexMap directDexMap) {
		this.wrappedMapBuilder = wrappedMapBuilder;
		this.directDexMap = directDexMap;
	}

	@Override
	public MemberMapBuilder addClassMapping(String name, String newName) {
		try {
			final MemberMapBuilder wrappedMemberMapBuilder = wrappedMapBuilder.addClassMapping(newName, name);
			return new MemberMapBuilder() {
				@Override
				public void addFieldMapping(String type, String name, String newName) {
					try {
						String mappedType = DexMaps.mapType(type, directDexMap);
						wrappedMemberMapBuilder.addFieldMapping(mappedType, newName, name);
					} catch (BuilderException e) {
						throw new BuilderException(EXCEPTION_HEADER + e.getMessage());
					}
				}
				@Override
				public void addMethodMapping(String[] parameterTypes, String returnType, String name, String newName) {
					try {
						int length = parameterTypes.length;
						String[] mappedParameterTypes = new String[length];
						for (int i = 0; i < length; i++) {
							mappedParameterTypes[i] = DexMaps.mapType(parameterTypes[i], directDexMap);
						}
						wrappedMemberMapBuilder.addMethodMapping(mappedParameterTypes, returnType, newName, name);
					} catch (BuilderException e) {
						throw new BuilderException(EXCEPTION_HEADER + e.getMessage());
					}
				}
			};
		} catch (BuilderException e) {
			throw new BuilderException(EXCEPTION_HEADER + e.getMessage());
		}
	}

}
