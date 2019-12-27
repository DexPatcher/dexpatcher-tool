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

import lanchon.dexpatcher.core.util.TypeName;

public class TypeDescriptorMapBuilder implements MapBuilder {

	protected final MapBuilder wrappedMapBuilder;

	public TypeDescriptorMapBuilder(MapBuilder wrappedMapBuilder) {
		this.wrappedMapBuilder = wrappedMapBuilder;
	}

	@Override
	public MemberMapBuilder addClassMapping(String name, String newName) {
		name = TypeName.toClassDescriptor(name);
		newName = TypeName.toClassDescriptor(newName);
		final MemberMapBuilder wrappedMemberMapBuilder = wrappedMapBuilder.addClassMapping(name, newName);
		return new MemberMapBuilder() {
			@Override
			public void addFieldMapping(String type, String name, String newName) {
				type = TypeName.toFieldDescriptor(type);
				wrappedMemberMapBuilder.addFieldMapping(type, name, newName);
			}
			@Override
			public void addMethodMapping(String[] parameterTypes, String returnType, String name, String newName) {
				int length = parameterTypes.length;
				for (int i = 0; i < length; i++) {
					parameterTypes[i] = TypeName.toFieldDescriptor(parameterTypes[i]);
				}
				returnType = TypeName.toReturnDescriptor(returnType);
				wrappedMemberMapBuilder.addMethodMapping(parameterTypes, returnType, name, newName);
			}
		};
	}

}
