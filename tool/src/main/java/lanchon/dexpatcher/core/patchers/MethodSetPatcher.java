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
import java.util.Collection;
import java.util.List;

import lanchon.dexpatcher.core.Action;
import lanchon.dexpatcher.core.Context;
import lanchon.dexpatcher.core.Marker;
import lanchon.dexpatcher.core.PatchException;
import lanchon.dexpatcher.core.PatcherAnnotation;
import lanchon.dexpatcher.core.model.BasicMethod;
import lanchon.dexpatcher.core.model.BasicMethodImplementation;
import lanchon.dexpatcher.core.util.DexUtils;
import lanchon.dexpatcher.core.util.Id;
import lanchon.dexpatcher.core.util.Label;

import org.jf.dexlib2.Opcode;
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
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction;
import org.jf.dexlib2.immutable.instruction.ImmutableInstructionFactory;
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.rewriter.InstructionRewriter;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.Rewriters;
import org.jf.dexlib2.util.MethodUtil;
import org.jf.dexlib2.util.TypeUtils;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;
import static org.jf.dexlib2.AccessFlags.*;
import static org.jf.dexlib2.Opcode.*;

public class MethodSetPatcher extends MemberSetPatcher<Method> {

	private Method sourceFileMethod;
	private int sourceFileLine;

	private boolean staticConstructorFound;

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
		setupLogPrefix(getSetItemLabel() + " '" + Label.ofMethod(item) + "'");
		setSourceFileMethod(patch);
	}

	// Implementation

	@Override
	public Collection<Method> process(Iterable<? extends Method> sourceSet, int sourceSetSizeHint,
			Iterable<? extends Method> patchSet, int patchSetSizeHint) {
		staticConstructorFound = false;
		Collection<Method> methods = super.process(sourceSet, sourceSetSizeHint, patchSet, patchSetSizeHint);
		if (staticConstructorAction != null && !staticConstructorFound) {
			log(ERROR, "static constructor not found");
		}
		return methods;
	}

	@Override
	protected final String getId(Method item) {
		return Id.ofMethod(item);
	}

	@Override
	protected String getSetItemLabel() {
		return "method";
	}

	@Override
	protected String getSetItemShortLabel() {
		return "method";
	}

	@Override
	protected Action getDefaultAction(String patchId, Method patch) throws PatchException {
		if (DexUtils.isStaticConstructor(patchId, patch)) {
			staticConstructorFound = true;
			if (staticConstructorAction != null) return staticConstructorAction;
			if (defaultAction == null) {
				Action action = targetExists(Id.STATIC_CONSTRUCTOR) ? Action.APPEND : Action.ADD;
				log(INFO, "implicit " + action.getLabel() + " of static constructor");
				return action;
			}
		} else if (DexUtils.isDefaultConstructor(patchId, patch) && defaultAction == null &&
				!getContext().isConstructorAutoIgnoreDisabled()) {
			if (DexUtils.hasTrivialConstructorImplementation(patch)) {
				log(INFO, "implicit ignore of trivial default constructor");
				return Action.IGNORE;
			}
			throw new PatchException("no action defined for non-trivial default constructor");
		}
		return super.getDefaultAction(patchId, patch);
	}

	@Override
	protected String getTargetId(String patchId, Method patch, PatcherAnnotation annotation) {
		String target = annotation.getTarget();
		String resolvedTarget = (target != null ? target : patch.getName());
		String targetId;
		String targetLabel;
		if (isTaggedByLastParameter(patch, true)) {
			ArrayList<MethodParameter> parameters = new ArrayList<>(patch.getParameters());
			parameters.remove(parameters.size() - 1);
			targetId = Id.ofMethod(parameters, patch.getReturnType(), resolvedTarget);
			targetLabel = Label.ofMethod(parameters, patch.getReturnType(), resolvedTarget);
		} else {
			targetId = (target != null ? Id.ofMethod(patch, target) : patchId);
			targetLabel = Label.shortLabelOfMember(resolvedTarget);
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
	protected Method onSimpleEdit(Method patch, PatcherAnnotation annotation, Method target, boolean inPlace) {

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

		return super.onSimpleEdit(patched, annotation, target, inPlace);

	}

	@Override
	protected Method onSimpleReplace(Method patch, PatcherAnnotation annotation, Method target, boolean inPlace) {
		return onSimpleAdd(patch, annotation);
	}

	// Wrap

	@Override
	protected void onWrap(String patchId, Method patch, PatcherAnnotation annotation) throws PatchException {

		if (DexUtils.isStaticConstructor(patchId, patch) || DexUtils.isInstanceConstructor(patchId, patch)) {
			throw Action.WRAP.invalidAction();
		}

		Method target = findTargetNonNative(patchId, patch, annotation);

		Method wrapSource = new BasicMethod(
				target.getDefiningClass(),
				createMethodName(patch, Marker.WRAP_SOURCE_SUFFIX),
				target.getParameters(),
				target.getReturnType(),
				createMethodFlags(target),
				target.getAnnotations(),
				target.getImplementation());
		addPatched(patch, wrapSource);

		Method wrapMain = new BasicMethod(
				patch.getDefiningClass(),
				patch.getName(),
				patch.getParameters(),
				patch.getReturnType(),
				patch.getAccessFlags(),
				annotation.getFilteredAnnotations(),
				replaceMethodInvocations(patch.getImplementation(), patch, wrapSource));
		addPatched(patch, wrapMain);

	}

	// Prepend and Append

	@Override
	protected void onSplice(String patchId, Method patch, PatcherAnnotation annotation, Action action)
			throws PatchException {

		if (DexUtils.isInstanceConstructor(patchId, patch)) {
			throw action.invalidAction();
		}

		if (!"V".equals(patch.getReturnType())) {
			throw new PatchException(action.getLabel() + " action can only be applied to methods that return void");
		}
		boolean prepend = (action == Action.PREPEND);
		Method target = findTargetNonNative(patchId, patch, annotation);

		Method spliceSource = new BasicMethod(
				target.getDefiningClass(),
				createMethodName(patch, prepend ? Marker.PREPEND_SOURCE_SUFFIX : Marker.APPEND_SOURCE_SUFFIX),
				target.getParameters(),
				target.getReturnType(),
				createMethodFlags(target),
				target.getAnnotations(),
				target.getImplementation());
		addPatched(patch, spliceSource);

		Method splicePatch = new BasicMethod(
				patch.getDefiningClass(),
				createMethodName(patch, prepend ? Marker.PREPEND_PATCH_SUFFIX : Marker.APPEND_PATCH_SUFFIX),
				patch.getParameters(),
				patch.getReturnType(),
				createMethodFlags(patch),
				annotation.getFilteredAnnotations(),
				patch.getImplementation());
		addPatched(patch, splicePatch);

		Method spliceMain = new BasicMethod(
				patch.getDefiningClass(),
				patch.getName(),
				patch.getParameters(),
				patch.getReturnType(),
				patch.getAccessFlags(),
				annotation.getFilteredAnnotations(),
				createCallSequence(MethodUtil.getParameterRegisterCount(patch),
						prepend ? splicePatch : spliceSource,
						prepend ? spliceSource : splicePatch));
		addPatched(patch, spliceMain);

	}

	// Helpers

	private Method findTargetNonNative(String patchId, Method patch, PatcherAnnotation annotation)
			throws PatchException {
		if (NATIVE.isSet(patch.getAccessFlags())) throw new PatchException("patch method is native");
		String targetId = getTargetId(patchId, patch, annotation);
		Method target = findTarget(targetId, false);
		if (NATIVE.isSet(target.getAccessFlags())) throw new PatchException("target method is native");
		return target;
	}

	private String createMethodName(Method base, String suffix) {
		String prefix = base.getName();
		int pl = prefix.length();
		if (pl >= 2 && prefix.charAt(0) == '<' && prefix.charAt(pl - 1) == '>') {
			prefix = Marker.SPECIAL_METHOD_PREFIX + prefix.substring(1, pl - 1);
		}
		String baseName = prefix + suffix;
		int n = 1;
		String name = baseName;
		for (;;) {
			if (!targetExists(Id.ofMethod(base, name))) return name;
			n++;
			name = baseName + n;
		}
	}

	private static int createMethodFlags(Method method) {
		int flags = method.getAccessFlags();
		if (MethodUtil.isDirect(method)) {
			flags &= ~(PUBLIC.getValue() | PROTECTED.getValue());
			flags |= PRIVATE.getValue();
		} else {
			if (!PRIVATE.isSet(flags)) {
				flags &= ~PUBLIC.getValue();
				flags |= PROTECTED.getValue();
			}
		}
		flags &= ~CONSTRUCTOR.getValue();
		return flags;
	}

	private MethodImplementation replaceMethodInvocations(MethodImplementation implementation,
			final Method from, final Method to) {

		final boolean fromIsDirect = MethodUtil.isDirect(from);

		DexRewriter rewriter = new DexRewriter(new RewriterModule() {
			@Override
			public Rewriter<Instruction> getInstructionRewriter(Rewriters rewriters) {
				return new InstructionRewriter(rewriters) {

					@Override
					public Instruction rewrite(Instruction instruction) {
						if (!(instruction instanceof ReferenceInstruction)) return instruction;
						Reference reference = ((ReferenceInstruction) instruction).getReference();
						if (!from.equals(reference)) return instruction;
						boolean invokeIsDirect;
						switch (instruction.getOpcode()) {
							case INVOKE_DIRECT:
							case INVOKE_DIRECT_RANGE:
							case INVOKE_STATIC:
							case INVOKE_STATIC_RANGE:
								invokeIsDirect = true;
								break;
							case INVOKE_VIRTUAL:
							case INVOKE_VIRTUAL_RANGE:
								invokeIsDirect = false;
								break;
							case INVOKE_SUPER:
							case INVOKE_SUPER_RANGE:
							case INVOKE_INTERFACE:
							case INVOKE_INTERFACE_RANGE:
								invokeIsDirect = false;
								if (fromIsDirect == invokeIsDirect) {
									log(ERROR, "unsupported invocation type (" + instruction.getOpcode() + ")");
									return instruction;
								}
								break;
							default:
								return instruction;
						}
						if (fromIsDirect != invokeIsDirect) {
							log(ERROR, "unexpected invocation type (" + instruction.getOpcode() + ")");
							return instruction;
						}
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
						} else {
							log(ERROR, "unexpected instruction type (" + instruction.getClass().getSimpleName() + ")");
							return instruction;
						}
					}

				};
			}
		});

		return rewriter.getMethodImplementationRewriter().rewrite(implementation);

	}

	private static MethodImplementation createCallSequence(int parameterCount, Method... methods) throws PatchException {
		ImmutableInstructionFactory factory = ImmutableInstructionFactory.INSTANCE;
		List<ImmutableInstruction> instructions = new ArrayList<>(methods.length + 1);
		for (Method method : methods) {
			Opcode opcode = getInvokeOpcode(method.getAccessFlags(), true);
			instructions.add(factory.makeInstruction3rc(opcode, 0, parameterCount, method));
		}
		instructions.add(factory.makeInstruction10x(RETURN_VOID));
		return new ImmutableMethodImplementation(parameterCount, instructions, null, null);
	}

	private static Opcode getInvokeOpcode(int methodFlags, boolean range) throws PatchException {
		if (CONSTRUCTOR.isSet(methodFlags)) throw new PatchException("constructor invocation is not supported");
		if (STATIC.isSet(methodFlags)) return range ? INVOKE_STATIC_RANGE : INVOKE_STATIC;
		if (PRIVATE.isSet(methodFlags)) return range ? INVOKE_DIRECT_RANGE : INVOKE_DIRECT;
		return range ? INVOKE_VIRTUAL_RANGE : INVOKE_VIRTUAL;
	}

}
