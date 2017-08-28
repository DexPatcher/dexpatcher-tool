/*
 * DexPatcher - Copyright 2015-2017 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core.patchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lanchon.dexpatcher.core.Action;
import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.core.Marker;
import lanchon.dexpatcher.core.PatchException;
import lanchon.dexpatcher.core.PatcherAnnotation;
import lanchon.dexpatcher.core.Util;
import lanchon.dexpatcher.core.model.BasicMethod;
import lanchon.dexpatcher.core.model.BasicMethodImplementation;

import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.debug.DebugItem;
import org.jf.dexlib2.iface.debug.LineNumber;
import org.jf.dexlib2.iface.debug.SetSourceFile;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction35c;
import org.jf.dexlib2.iface.instruction.formats.Instruction3rc;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.rewriter.InstructionRewriter;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.Rewriters;
import org.jf.dexlib2.util.TypeUtils;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;
import static org.jf.dexlib2.AccessFlags.*;

public abstract class MethodSetPatcher extends MemberSetPatcher<Method> {

	private Method sourceFileMethod;
	private int sourceFileLine;

	public MethodSetPatcher(ClassSetPatcher parent, PatcherAnnotation annotation) {
		super(parent, annotation);
	}

	// Debug Info

	protected void setSourceFileMethod(Method sourceFileMethod) {
		this.sourceFileMethod = sourceFileMethod;
		sourceFileLine = 0;
	}

	@Override
	protected int getSourceFileLine() {
		// Parse debug information lazily.
		if (sourceFileMethod != null) {
			MethodImplementation mi = sourceFileMethod.getImplementation();
			if (mi != null) {
				for (DebugItem di : mi.getDebugItems()) {
					if (di instanceof LineNumber) {
						sourceFileLine = ((LineNumber) di).getLineNumber();
						break;
					}
					if (di instanceof SetSourceFile) {
						// TODO: Support this type of debug item.
						break;
					}
				}
			}
			sourceFileMethod = null;
		}
		return sourceFileLine;
	}

	// Logging

	@Override
	protected void clearLogPrefix() {
		super.clearLogPrefix();
		setSourceFileMethod(null);
	}

	@Override
	protected void setupLogPrefix(String id, Method item, Method patch, Method patched) {
		setupLogPrefix(getSetItemLabel() + " '" + Util.getMethodLabel(item) + "'");
		setSourceFileMethod(patch);
	}

	// Implementation

	@Override
	protected final String getId(Method item) {
		return Util.getMethodId(item);
	}

	@Override
	protected String getSetItemShortLabel() {
		return "method";
	}

	@Override
	protected String getTargetId(String patchId, Method patch, PatcherAnnotation annotation) {
		String target = annotation.getTarget();
		String resolvedTarget = (target != null ? target : patch.getName());
		String targetId;
		String targetLabel;
		if (isTaggedByLastParameter(patch, true)) {
			ArrayList<MethodParameter> parameters = new ArrayList<MethodParameter>(patch.getParameters());
			parameters.remove(parameters.size() - 1);
			targetId = Util.getMethodId(parameters, patch.getReturnType(), resolvedTarget);
			targetLabel = Util.getMethodLabel(parameters, patch.getReturnType(), resolvedTarget);
		} else {
			targetId = (target != null ? Util.getMethodId(patch, target) : patchId);
			targetLabel = Util.getMemberShortLabel(resolvedTarget);
		}
		if (shouldLogTarget(patchId, targetId)) {
			extendLogPrefixWithTargetLabel(targetLabel);
		}
		return targetId;
	}

	private boolean isTaggedByLastParameter(Method patch, boolean warn) {
		List<? extends MethodParameter> parameters = patch.getParameters();
		int size = parameters.size();
		if (size == 0) return false;
		MethodParameter lastParameter = parameters.get(parameters.size() - 1);
		Context context = getContext();
		if (context.getTagTypeDescriptor().equals(lastParameter.getType())) {
			if (context.isDexTagSupported()) return true;
			if (warn) log(WARN, "use of deprecated DexTag detected (consider enabling DexTag support)");
		}
		for (Annotation annotation : lastParameter.getAnnotations()) {
			if (context.getActionFromMarkerTypeDescriptor(annotation.getType()) == Action.IGNORE) return true;
		}
		return false;
	}

	@Override
	protected Method onSimpleAdd(Method patch, PatcherAnnotation annotation) {

		// Avoid creating a new object if not necessary.
		if (patch.getAnnotations() == annotation.getFilteredAnnotations()) {
			return patch;
		}

		return new BasicMethod(
				patch.getDefiningClass(),
				patch.getName(),
				patch.getParameters(),
				patch.getReturnType(),
				patch.getAccessFlags(),
				annotation.getFilteredAnnotations(),
				patch.getImplementation());

	}

	@Override
	protected Method onSimpleEdit(Method patch, PatcherAnnotation annotation, Method target, boolean inPlaceEdit) {

		//String message = "updating '%s' modifier in edited member to match its target";
		//AccessFlags[] flagArray = new AccessFlags[] { CONSTRUCTOR };
		//int flagMask = CONSTRUCTOR.getValue();
		//int patchFlags = patch.getAccessFlags();
		//int targetFlags = target.getAccessFlags();
		//checkAccessFlags(INFO, patchFlags, targetFlags, flagArray, message);
		//int flags = (patchFlags & ~flagMask) | (targetFlags & flagMask); 

		MethodImplementation implementation = target.getImplementation();
		if (isTaggedByLastParameter(patch, false)) {
			List<? extends MethodParameter> parameters = patch.getParameters();
			MethodParameter lastParameter = parameters.get(parameters.size() - 1);
			int tagRegisterCount = (TypeUtils.isWideType(lastParameter) ? 2 : 1);
			implementation = new BasicMethodImplementation(
					implementation.getRegisterCount() + tagRegisterCount,
					implementation.getInstructions(),
					implementation.getTryBlocks(),
					implementation.getDebugItems());
		}

		Method patched = new BasicMethod(
				patch.getDefiningClass(),
				patch.getName(),
				patch.getParameters(),
				patch.getReturnType(),
				patch.getAccessFlags(),
				annotation.getFilteredAnnotations(),
				implementation);

		return super.onSimpleEdit(patched, annotation, target, inPlaceEdit);

	}

	// Wrap

	@Override
	protected void onWrap(String patchId, Method patch, PatcherAnnotation annotation) throws PatchException {
		String targetId = getTargetId(patchId, patch, annotation);
		Method target = findTarget(targetId, false);
		Method wrapSource = onSimpleWrapSource(patch, annotation, target);
		String wrapSourceId = Util.getMethodId(wrapSource);
		addPatched(wrapSourceId, patch, wrapSource);
		Method wrapPatch = onSimpleWrapPatch(patch, annotation, wrapSource);
		addPatched(patchId, patch, wrapPatch);
	}

	private Method onSimpleWrapSource(Method patch, PatcherAnnotation annotation, Method target) {
		return new BasicMethod(
				target.getDefiningClass(),
				createMethodName(patch, Marker.WRAP_SOURCE_SUFFIX),
				target.getParameters(),
				target.getReturnType(),
				limitMethodAccess(target.getAccessFlags()),
				target.getAnnotations(),
				target.getImplementation());
	}

	private Method onSimpleWrapPatch(Method patch, PatcherAnnotation annotation, Method wrapSource) {
		return new BasicMethod(
				patch.getDefiningClass(),
				patch.getName(),
				patch.getParameters(),
				patch.getReturnType(),
				patch.getAccessFlags(),
				annotation.getFilteredAnnotations(),
				replaceMethodInvocations(patch.getImplementation(), patch, wrapSource));
	}

	// Helpers

	private String createMethodName(Method base, String suffix) {
		Map<String, Method> sourceMap = getSourceMap();
		String baseName = base.getName() + suffix;
		int n = 1;
		String name = baseName;
		for (;;) {
			String id = Util.getMethodId(base, name);
			if (sourceMap.get(id) == null) return name;
			n++;
			name = baseName + n;
		}
	}

	private int limitMethodAccess(int accessFlags) {
		if (this instanceof VirtualMethodSetPatcher) {
			if (!PRIVATE.isSet(accessFlags)) {
				accessFlags &= ~PUBLIC.getValue();
				accessFlags |= PROTECTED.getValue();
			}
		} else {
			accessFlags &= ~(PUBLIC.getValue() | PROTECTED.getValue());
			accessFlags |= PRIVATE.getValue();
		}
		return accessFlags;
	}

	private MethodImplementation replaceMethodInvocations(MethodImplementation implementation,
			final Method from, final Method to) {

		final boolean virtual = this instanceof VirtualMethodSetPatcher;

		DexRewriter rewriter = new DexRewriter(new RewriterModule() {
			public Rewriter<Instruction> getInstructionRewriter(Rewriters rewriters) {
				return new InstructionRewriter(rewriters) {

					@Override
					public Instruction rewrite(Instruction instruction) {
						if (instruction instanceof ReferenceInstruction) {
							Reference reference = ((ReferenceInstruction) instruction).getReference();
							if (from.equals(reference)) {
								boolean match;
								switch (instruction.getOpcode()) {
									case INVOKE_DIRECT:
									case INVOKE_DIRECT_RANGE:
									case INVOKE_STATIC:
									case INVOKE_STATIC_RANGE:
										match = !virtual;
										break;
									case INVOKE_VIRTUAL:
									case INVOKE_VIRTUAL_RANGE:
									//case INVOKE_SUPER:
									//case INVOKE_SUPER_RANGE:
									//case INVOKE_INTERFACE:
									//case INVOKE_INTERFACE_RANGE:
										match = virtual;
										break;
									default:
										match = false;
								}
								if (match) {
									if (instruction instanceof Instruction35c) {
										return new RewrittenInstruction35c((Instruction35c) instruction) {
											//@Override
											//public Opcode getOpcode() {
											//	return virtual ? instruction.getOpcode() :
											//			STATIC.isSet(to.getAccessFlags()) ?
											//					INVOKE_STATIC : INVOKE_DIRECT;
											//}
											@Override
											public Reference getReference() {
												return to;
											}
										};
									} else if (instruction instanceof Instruction3rc) {
										return new RewrittenInstruction3rc((Instruction3rc) instruction) {
											//@Override
											//public Opcode getOpcode() {
											//	return virtual ? instruction.getOpcode() :
											//			STATIC.isSet(to.getAccessFlags()) ?
											//					INVOKE_STATIC_RANGE : INVOKE_DIRECT_RANGE;
											//}
											@Override
											public Reference getReference() {
												return to;
											}
										};
									} else throw new AssertionError("Unexpected instruction");
								}
							}
						}
						return instruction;
					}

				};
			}
		});

		return rewriter.getMethodImplementationRewriter().rewrite(implementation);

	}

}
