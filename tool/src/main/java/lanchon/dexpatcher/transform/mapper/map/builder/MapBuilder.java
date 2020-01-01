/*
 * DexPatcher - Copyright 2015-2020 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.mapper.map.builder;

public interface MapBuilder {

	MemberMapBuilder addClassMapping(String name, String newName);

	interface MemberMapBuilder {
		void addFieldMapping(String type, String name, String newName);
		void addMethodMapping(String[] parameterTypes, String returnType, String name, String newName);
	}

	class BuilderException extends RuntimeException {
		public BuilderException(String s) {
			super(s);
		}
	}

}
