/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform;

import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.ExceptionHandler;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.TryBlock;
import org.jf.dexlib2.iface.debug.DebugItem;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.immutable.debug.ImmutableDebugItem;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction;
import org.jf.dexlib2.immutable.value.ImmutableEncodedValueFactory;

public class DexVisitor {

	public void visitDexFile(DexFile dexFile) {
		dexFile.getOpcodes();
		visitClassDefs(dexFile.getClasses());
	}

	public void visitClassDef(ClassDef classDef) {
		visitType(classDef.getType());
		classDef.getAccessFlags();
		visitType(classDef.getSuperclass());
		visitTypes(classDef.getInterfaces());
		classDef.getSourceFile();
		visitAnnotations(classDef.getAnnotations());
		visitFields(classDef.getStaticFields());
		visitFields(classDef.getInstanceFields());
		visitMethods(classDef.getDirectMethods());
		visitMethods(classDef.getVirtualMethods());
	}

	public void visitType(String type) {}

	public void visitAnnotation(Annotation annotation) {
		annotation.getVisibility();
		visitType(annotation.getType());
		visitAnnotationElements(annotation.getElements());
	}

	public void visitAnnotationElement(AnnotationElement annotationElement) {
		annotationElement.getName();
		visitEncodedValue(annotationElement.getValue());
	}

	public void visitEncodedValue(EncodedValue encodedValue) {
		ImmutableEncodedValueFactory.of(encodedValue);
	}

	public void visitField(Field field) {
		visitType(field.getDefiningClass());
		field.getName();
		visitType(field.getType());
		field.getAccessFlags();
		EncodedValue initialValue = field.getInitialValue();
		if (initialValue != null) visitEncodedValue(initialValue);
		visitAnnotations(field.getAnnotations());
	}

	public void visitMethod(Method method) {
		visitType(method.getDefiningClass());
		method.getName();
		visitMethodParameters(method.getParameters());
		visitType(method.getReturnType());
		method.getAccessFlags();
		visitAnnotations(method.getAnnotations());
		MethodImplementation implementation = method.getImplementation();
		if (implementation != null) visitMethodImplementation(implementation);
	}

	public void visitMethodParameter(MethodParameter methodParameter) {
		visitType(methodParameter.getType());
		visitAnnotations(methodParameter.getAnnotations());
		methodParameter.getName();
	}

	public void visitMethodImplementation(MethodImplementation methodImplementation) {
		methodImplementation.getRegisterCount();
		visitInstructions(methodImplementation.getInstructions());
		visitTryBlocks(methodImplementation.getTryBlocks());
		visitDebugItems(methodImplementation.getDebugItems());
	}

	public void visitInstruction(Instruction instruction) {
		ImmutableInstruction.of(instruction);
	}

	public void visitTryBlock(TryBlock<? extends ExceptionHandler> tryBlock) {
		tryBlock.getStartCodeAddress();
		tryBlock.getCodeUnitCount();
		visitExceptionHandlers(tryBlock.getExceptionHandlers());
	}

	public void visitExceptionHandler(ExceptionHandler exceptionHandler) {
		visitType(exceptionHandler.getExceptionType());
		exceptionHandler.getHandlerCodeAddress();
	}

	public void visitDebugItem(DebugItem debugItem) {
		ImmutableDebugItem.of(debugItem);
	}

	// Collection Helpers

	public final void visitClassDefs(Iterable<? extends ClassDef> classDefs) {
		for (ClassDef classDef : classDefs) visitClassDef(classDef);
	}

	public final void visitTypes(Iterable<String> types) {
		for (String type : types) visitType(type);
	}

	public final void visitAnnotations(Iterable<? extends Annotation> annotations) {
		for (Annotation annotation : annotations) visitAnnotation(annotation);
	}

	public final void visitAnnotationElements(Iterable<? extends AnnotationElement> annotationElements) {
		for (AnnotationElement element : annotationElements) visitAnnotationElement(element);
	}

	public final void visitFields(Iterable<? extends Field> fields) {
		for (Field field : fields) visitField(field);
	}

	public final void visitMethods(Iterable<? extends Method> methods) {
		for (Method method : methods) visitMethod(method);
	}

	public final void visitMethodParameters(Iterable<? extends MethodParameter> methodParameters) {
		for (MethodParameter parameter : methodParameters) visitMethodParameter(parameter);
	}

	public final void visitInstructions(Iterable<? extends Instruction> instructions) {
		for (Instruction instruction : instructions) visitInstruction(instruction);
	}

	public final void visitTryBlocks(Iterable<? extends TryBlock<? extends ExceptionHandler>> tryBlocks) {
		for (TryBlock<? extends ExceptionHandler> tryBlock : tryBlocks) visitTryBlock(tryBlock);
	}

	public final void visitExceptionHandlers(Iterable<? extends ExceptionHandler> exceptionHandlers) {
		for (ExceptionHandler exceptionHandler : exceptionHandlers) visitExceptionHandler(exceptionHandler);
	}

	public final void visitDebugItems(Iterable<? extends DebugItem> debugItems) {
		for (DebugItem debugItem : debugItems) visitDebugItem(debugItem);

	}

}
