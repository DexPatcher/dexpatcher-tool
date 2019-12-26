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

public class InverseMapBuilder implements MapBuilder {

	private static final String EXCEPTION_HEADER = "inverse map: ";

	public static MapBuilder of(MapBuilder wrappedMapBuilder, boolean invert) {
		return invert ? new InverseMapBuilder(wrappedMapBuilder) : wrappedMapBuilder;
	}

	protected final MapBuilder wrappedMapBuilder;

	public InverseMapBuilder(MapBuilder wrappedMapBuilder) {
		this.wrappedMapBuilder = wrappedMapBuilder;
	}

	@Override
	public MemberMapBuilder addClassMapping(String name, String newName) {
		try {
			final MemberMapBuilder wrappedMemberMapBuilder = wrappedMapBuilder.addClassMapping(newName, name);
			return new MemberMapBuilder() {
				@Override
				public void addFieldMapping(String type, String name, String newName) {
					try {
						wrappedMemberMapBuilder.addFieldMapping(type, newName, name);
					} catch (BuilderException e) {
						throw new BuilderException(EXCEPTION_HEADER + e.getMessage());
					}
				}
				@Override
				public void addMethodMapping(String[] parameterTypes, String returnType, String name, String newName) {
					try {
						wrappedMemberMapBuilder.addMethodMapping(parameterTypes, returnType, newName, name);
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
