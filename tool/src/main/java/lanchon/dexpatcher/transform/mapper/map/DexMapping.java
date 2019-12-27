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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lanchon.dexpatcher.core.util.Id;
import lanchon.dexpatcher.transform.mapper.map.builder.MapBuilder;

import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;

public class DexMapping implements MapBuilder, DexMap {

	protected static class ClassMapping implements MemberMapBuilder {

		protected final String mapping;

		protected final Map<String, String> fieldMappings = new HashMap<>();
		protected final Map<String, String> methodMappings = new HashMap<>();

		public ClassMapping(String mapping) {
			this.mapping = mapping;
		}

		public String getMapping() {
			return mapping;
		}

		@Override
		public void addFieldMapping(String type, String name, String newName) {
			if (newName == null) throw new NullPointerException("newName");
			String id = Id.ofField(type, name);
			String currentName = fieldMappings.put(id, newName);
			if (currentName != null) {
				fieldMappings.put(id, currentName);
				throw new BuilderException("duplicate field mapping");
			}
		}

		public String getFieldMapping(String fieldId) {
			return fieldMappings.get(fieldId);
		}

		@Override
		public void addMethodMapping(String[] parameterTypes, String returnType, String name, String newName) {
			if (newName == null) throw new NullPointerException("newName");
			String id = Id.ofMethod(Arrays.asList(parameterTypes), returnType, name);
			String currentName = methodMappings.put(id, newName);
			if (currentName != null) {
				methodMappings.put(id, currentName);
				throw new BuilderException("duplicate method mapping");
			}
		}

		public String getMethodMapping(String methodId) {
			return methodMappings.get(methodId);
		}

	}

	protected final Map<String, ClassMapping> classMappings = new HashMap<>();

	@Override
	public MemberMapBuilder addClassMapping(String name, String newName) {
		if (newName == null) throw new NullPointerException("newName");
		String id = Id.ofClass(name);
		ClassMapping newMapping = new ClassMapping(newName);
		ClassMapping currentMapping = classMappings.put(id, newMapping);
		if (currentMapping != null) {
			classMappings.put(id, currentMapping);
			throw new BuilderException("duplicate type mapping");
		}
		return newMapping;
	}

	@Override
	public String getClassMapping(String descriptor) {
		return getClassMappingById(Id.ofClass(descriptor));
	}

	@Override
	public String getFieldMapping(FieldReference field) {
		return getFieldMappingById(Id.ofClass(field.getDefiningClass()), Id.ofField(field));
	}

	@Override
	public String getMethodMapping(MethodReference method) {
		return getMethodMappingById(Id.ofClass(method.getDefiningClass()), Id.ofMethod(method));

	}

	protected final String getClassMappingById(String classId) {
		ClassMapping classMapping = classMappings.get(classId);
		return classMapping != null ? classMapping.getMapping() : null;
	}

	protected final String getFieldMappingById(String classId, String fieldId) {
		ClassMapping classMapping = classMappings.get(classId);
		return classMapping != null ? classMapping.getFieldMapping(fieldId) : null;
	}

	protected final String getMethodMappingById(String classId, String methodId) {
		ClassMapping classMapping = classMappings.get(classId);
		return classMapping != null ? classMapping.getMethodMapping(methodId) : null;
	}

}
