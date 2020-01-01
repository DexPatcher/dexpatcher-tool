/*
 * DexPatcher - Copyright 2015-2020 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.util.wrapper;

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

public class WrapperRewriterModule<M extends RewriterModule> extends RewriterModule {

	protected final M wrappedModule;

	public WrapperRewriterModule(M wrappedModule) {
		this.wrappedModule = wrappedModule;
	}

	@Override
	public Rewriter<ClassDef> getClassDefRewriter(Rewriters rewriters) {
		return wrappedModule.getClassDefRewriter(rewriters);
	}

	@Override
	public Rewriter<Field> getFieldRewriter(Rewriters rewriters) {
		return wrappedModule.getFieldRewriter(rewriters);
	}

	@Override
	public Rewriter<Method> getMethodRewriter(Rewriters rewriters) {
		return wrappedModule.getMethodRewriter(rewriters);
	}

	@Override
	public Rewriter<MethodParameter> getMethodParameterRewriter(Rewriters rewriters) {
		return wrappedModule.getMethodParameterRewriter(rewriters);
	}

	@Override
	public Rewriter<MethodImplementation> getMethodImplementationRewriter(Rewriters rewriters) {
		return wrappedModule.getMethodImplementationRewriter(rewriters);
	}

	@Override
	public Rewriter<Instruction> getInstructionRewriter(Rewriters rewriters) {
		return wrappedModule.getInstructionRewriter(rewriters);
	}

	@Override
	public Rewriter<TryBlock<? extends ExceptionHandler>> getTryBlockRewriter(Rewriters rewriters) {
		return wrappedModule.getTryBlockRewriter(rewriters);
	}

	@Override
	public Rewriter<ExceptionHandler> getExceptionHandlerRewriter(Rewriters rewriters) {
		return wrappedModule.getExceptionHandlerRewriter(rewriters);
	}

	@Override
	public Rewriter<DebugItem> getDebugItemRewriter(Rewriters rewriters) {
		return wrappedModule.getDebugItemRewriter(rewriters);
	}

	@Override
	public Rewriter<String> getTypeRewriter(Rewriters rewriters) {
		return wrappedModule.getTypeRewriter(rewriters);
	}

	@Override
	public Rewriter<FieldReference> getFieldReferenceRewriter(Rewriters rewriters) {
		return wrappedModule.getFieldReferenceRewriter(rewriters);
	}

	@Override
	public Rewriter<MethodReference> getMethodReferenceRewriter(Rewriters rewriters) {
		return wrappedModule.getMethodReferenceRewriter(rewriters);
	}

	@Override
	public Rewriter<Annotation> getAnnotationRewriter(Rewriters rewriters) {
		return wrappedModule.getAnnotationRewriter(rewriters);
	}

	@Override
	public Rewriter<AnnotationElement> getAnnotationElementRewriter(Rewriters rewriters) {
		return wrappedModule.getAnnotationElementRewriter(rewriters);
	}

	@Override
	public Rewriter<EncodedValue> getEncodedValueRewriter(Rewriters rewriters) {
		return wrappedModule.getEncodedValueRewriter(rewriters);
	}

}
