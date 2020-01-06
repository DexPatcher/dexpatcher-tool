/*
 * DexPatcher - Copyright 2015-2020 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;

public class TemplateMapFileWriter {

	private static String MEMBER_INDENTATION = "    ";

	public static void write(File file, DexFile dexFile, String prefix) throws IOException {
		try (OutputStream outputStream = new FileOutputStream(file)) {
			Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
			write(writer, dexFile, prefix);
		}
	}

	public static void write(Writer writer, DexFile dexFile, String prefix) throws IOException {
		write(new BufferedWriter(writer), dexFile, prefix);
	}

	public static void write(BufferedWriter writer, DexFile dexFile, String prefix) throws IOException {
		write(new PrintWriter(writer), dexFile, prefix);
	}

	public static void write(PrintWriter writer, DexFile dexFile, String prefix) throws IOException {
		if (prefix == null) prefix = "";
		for (ClassDef classDef : dexFile.getClasses()) {
			String classDescriptor = classDef.getType();
			//if (DexUtils.isPackageDescriptor(classDescriptor)) continue;
			String className = Label.fromClassDescriptor(classDescriptor);
			writer.print(prefix);
			writer.print(className);
			writer.print(" -> ");
			writer.print(className);
			writer.print(":");
			writer.println();
			for (Field field : classDef.getFields()) {
				String fieldName = field.getName();
				writer.print(prefix);
				writer.print(MEMBER_INDENTATION);
				writer.print(Label.fromFieldDescriptor(field.getType()));
				writer.print(" ");
				writer.print(fieldName);
				writer.print(" -> ");
				writer.print(fieldName);
				writer.println();
			}
			for (Method method : classDef.getMethods()) {
				if (DexUtils.isStaticConstructor(method) || DexUtils.isInstanceConstructor(method)) continue;
				String methodName = method.getName();
				writer.print(prefix);
				writer.print(MEMBER_INDENTATION);
				writer.print(Label.fromReturnDescriptor(method.getReturnType()));
				writer.print(" ");
				writer.print(methodName);
				writer.print("(");
				boolean first = true;
				for (CharSequence parameterType : method.getParameterTypes()) {
					if (!first) writer.print(", ");
					writer.print(Label.fromFieldDescriptor(parameterType.toString()));
					first = false;
				}
				writer.print(") -> ");
				writer.print(methodName);
				writer.println();
			}
			writer.println();
		}
		if (writer.checkError()) throw new IOException("Cannot write template map file");
	}

}
