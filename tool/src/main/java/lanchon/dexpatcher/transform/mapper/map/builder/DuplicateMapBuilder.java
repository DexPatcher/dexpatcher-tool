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

public class DuplicateMapBuilder implements MapBuilder {

	protected final MapBuilder wrappedMapBuilder1;
	protected final MapBuilder wrappedMapBuilder2;

	public DuplicateMapBuilder(MapBuilder wrappedMapBuilder1, MapBuilder wrappedMapBuilder2) {
		this.wrappedMapBuilder1 = wrappedMapBuilder1;
		this.wrappedMapBuilder2 = wrappedMapBuilder2;
	}

	@Override
	public MemberMapBuilder addClassMapping(String name, String newName) {
		final MemberMapBuilder wrappedMemberMapBuilder1 = wrappedMapBuilder1.addClassMapping(name, newName);
		final MemberMapBuilder wrappedMemberMapBuilder2 = wrappedMapBuilder2.addClassMapping(name, newName);
		return new MemberMapBuilder() {
			@Override
			public void addFieldMapping(String type, String name, String newName) {
				wrappedMemberMapBuilder1.addFieldMapping(type, name, newName);
				wrappedMemberMapBuilder2.addFieldMapping(type, name, newName);
			}
			@Override
			public void addMethodMapping(String[] parameterTypes, String returnType, String name, String newName) {
				wrappedMemberMapBuilder1.addMethodMapping(parameterTypes, returnType, name, newName);
				wrappedMemberMapBuilder2.addMethodMapping(parameterTypes, returnType, name, newName);
			}
		};
	}

}
