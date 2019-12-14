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
import org.jf.dexlib2.iface.ExceptionHandler;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.TryBlock;
import org.jf.dexlib2.iface.debug.DebugItem;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.Rewriters;

public class DelegatorRewriterModule<M extends RewriterModule> extends RewriterModule {

	protected final M module;

	public DelegatorRewriterModule(M module) {
		this.module = module;
	}

	@Override
	public Rewriter<ClassDef> getClassDefRewriter(Rewriters rewriters) {
		return module.getClassDefRewriter(rewriters);
	}

	@Override
	public Rewriter<Field> getFieldRewriter(Rewriters rewriters) {
		return module.getFieldRewriter(rewriters);
	}

	@Override
	public Rewriter<Method> getMethodRewriter(Rewriters rewriters) {
		return module.getMethodRewriter(rewriters);
	}

	@Override
	public Rewriter<MethodParameter> getMethodParameterRewriter(Rewriters rewriters) {
		return module.getMethodParameterRewriter(rewriters);
	}

	@Override
	public Rewriter<MethodImplementation> getMethodImplementationRewriter(Rewriters rewriters) {
		return module.getMethodImplementationRewriter(rewriters);
	}

	@Override
	public Rewriter<Instruction> getInstructionRewriter(Rewriters rewriters) {
		return module.getInstructionRewriter(rewriters);
	}

	@Override
	public Rewriter<TryBlock<? extends ExceptionHandler>> getTryBlockRewriter(Rewriters rewriters) {
		return module.getTryBlockRewriter(rewriters);
	}

	@Override
	public Rewriter<ExceptionHandler> getExceptionHandlerRewriter(Rewriters rewriters) {
		return module.getExceptionHandlerRewriter(rewriters);
	}

	@Override
	public Rewriter<DebugItem> getDebugItemRewriter(Rewriters rewriters) {
		return module.getDebugItemRewriter(rewriters);
	}

	@Override
	public Rewriter<String> getTypeRewriter(Rewriters rewriters) {
		return module.getTypeRewriter(rewriters);
	}

	@Override
	public Rewriter<FieldReference> getFieldReferenceRewriter(Rewriters rewriters) {
		return module.getFieldReferenceRewriter(rewriters);
	}

	@Override
	public Rewriter<MethodReference> getMethodReferenceRewriter(Rewriters rewriters) {
		return module.getMethodReferenceRewriter(rewriters);
	}

	@Override
	public Rewriter<Annotation> getAnnotationRewriter(Rewriters rewriters) {
		return module.getAnnotationRewriter(rewriters);
	}

	@Override
	public Rewriter<AnnotationElement> getAnnotationElementRewriter(Rewriters rewriters) {
		return module.getAnnotationElementRewriter(rewriters);
	}

	@Override
	public Rewriter<EncodedValue> getEncodedValueRewriter(Rewriters rewriters) {
		return module.getEncodedValueRewriter(rewriters);
	}

}
