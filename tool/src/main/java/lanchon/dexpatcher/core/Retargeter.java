/*
 * DexPatcher - Copyright 2015-2017 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.core;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

import lanchon.dexpatcher.core.logger.Logger;
import lanchon.dexpatcher.core.model.Targets;
import lanchon.dexpatcher.core.patcher.ClassSetPatcher;
import lanchon.dexpatcher.core.patcher.FieldSetPatcher;
import lanchon.dexpatcher.core.patcher.MemberSetPatcher;
import lanchon.dexpatcher.core.patcher.MethodSetPatcher;

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Member;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.rewriter.FieldReferenceRewriter;
import org.jf.dexlib2.rewriter.FieldRewriter;
import org.jf.dexlib2.rewriter.MethodReferenceRewriter;
import org.jf.dexlib2.rewriter.MethodRewriter;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.Rewriters;
import org.jf.dexlib2.rewriter.TypeRewriter;

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public class Retargeter {

	Targets targets;
	Context context;
	Logger logger;

	private Map<String, String> subclasses;

	public Retargeter(Context context) {
		this.targets = new Targets();
		this.context = context;
		this.logger = context.getLogger();
		this.subclasses = new HashMap<>();
	}

	// Creates the source => target mapping for class types and fields/methods names,
	// this needs to be called before retartget()
	public void populateTargetMap(DexFile patchDex) throws PatchException {
		for (ClassDef patchClass : patchDex.getClasses()) {
			ClassSetPatcher classSetPatcher = new ClassSetPatcher(context);

			String classPatchId = classSetPatcher.getId(patchClass);

			PatcherAnnotation classActionContext = classSetPatcher.getActionContext(classPatchId, patchClass);

			if (!shouldRetarget(classActionContext)) {
				continue;
			}

			targets.addClass(classPatchId);

			// Keep track of subclasses so we can inherit the retargeting info for parent methods/fields
			if (patchClass.getSuperclass() != null) {
				subclasses.put(patchClass.getSuperclass(), classPatchId);
			}

			String classTargetId = classSetPatcher.getTargetId(classPatchId, patchClass, classActionContext);

			if (classPatchId != classTargetId) {
				log(INFO, "Mapping class " + classPatchId + " => " + classTargetId);
				targets.getTargetClass(classPatchId).target = classTargetId;
			}

			// Map class method and field names
			for (Method patchMethod : patchClass.getMethods()) {
				MethodSetPatcher methodSetPatcher = new MethodSetPatcher(classSetPatcher, classActionContext);

				targets.getTargetClass(classPatchId).fields.putAll(getTargetMapping(methodSetPatcher, patchMethod));
			}

			for (Field patchField : patchClass.getFields()) {
				FieldSetPatcher fieldSetPatcher = new FieldSetPatcher(classSetPatcher, classActionContext);

				targets.getTargetClass(classPatchId).fields.putAll(getTargetMapping(fieldSetPatcher, patchField));
			}
		}

		// Copy retargeting info to subclasses
		for (String parentClass : subclasses.keySet()) {
			log(DEBUG, "Copying " + targets.getTargetClass(parentClass).fields.size() + " field(s), "
					+ targets.getTargetClass(parentClass).methods.size() + " method(s) from "
					+ parentClass + " to " + subclasses.get(parentClass));
			copyTargetsToSubclasses(parentClass);
		}
	}

	private void copyTargetsToSubclasses(String parentClass) {
		String childClass = subclasses.get(parentClass);

		if (childClass == null) {
			return;
		}

		Targets.TargetClass parentClassDef = targets.getTargetClass(parentClass);
		Targets.TargetClass childClassDef = targets.getTargetClass(childClass);

		childClassDef.fields.putAll(parentClassDef.fields);
		childClassDef.methods.putAll(parentClassDef.methods);

		// Recurse
		copyTargetsToSubclasses(childClass);
	}

	// Rewrite all source references in the dex file using the target map
	public DexFile rewrite(DexFile patchDex) {
		
		DexRewriter rewriter = new DexRewriter(new RewriterModule() {
			
			// Field/method definitions
			@Override
			public Rewriter<Field> getFieldRewriter(@Nonnull Rewriters rewriters) {
				return new FieldRewriter(rewriters) {
					@Override
					public Field rewrite(final Field field) {
						return new RewrittenField(field) {
							@Override public String getName() {
								return targets.getRetargetedFieldName(field);
							}
						};
					}
				};
			}

			@Override
			public Rewriter<Method> getMethodRewriter(@Nonnull Rewriters rewriters) {
				return new MethodRewriter(rewriters) {
					@Override
					public Method rewrite(final Method method) {
						return new RewrittenMethod(method) {
							@Override public String getName() {
								return targets.getRetargetedMethodName(method);
							}
						};
					}
				};
			}

			// References to fields/methods in the method implementation
			@Override
			public Rewriter<FieldReference> getFieldReferenceRewriter(Rewriters rewriters) {
				return new FieldReferenceRewriter(rewriters) {

					@Override
					public FieldReference rewrite(final FieldReference field) {
						return new RewrittenFieldReference(field) {
							@Override public String getName() {
								return targets.getRetargetedFieldName(field);
							}
						};
					}
				};
			}

			@Override
			public Rewriter<MethodReference> getMethodReferenceRewriter(Rewriters rewriters) {
				return new MethodReferenceRewriter(rewriters) {

					@Override
					public MethodReference rewrite(final MethodReference method) {
						return new RewrittenMethodReference(method) {
							@Override public String getName() {
								return targets.getRetargetedMethodName(method);
							}
						};
					}
				};
			}

			// Rewrite class types everywhere (return types, method parameters, field types, etc)
			@Override
			public Rewriter<String> getTypeRewriter(Rewriters rewriters) {
				return new TypeRewriter() {
					@Override
					public String rewrite(String classType) {
						return targets.getRetargetedClassName(classType);
					}
				};
			}
		});

		return rewriter.rewriteDexFile(patchDex);
	}

	private <Member> Map<String, String> getTargetMapping(MemberSetPatcher<? super Member> memberPatcher, Member member) throws PatchException {
		String memberPatchId = memberPatcher.getId(member);
		PatcherAnnotation memberActionContext = memberPatcher.getActionContext(memberPatchId, member);
		HashMap<String, String> targetMap = new HashMap<>();

		if (!shouldRetarget(memberActionContext)) {
			return targetMap;
		}

		String fieldTargetId = memberPatcher.getTargetId(memberPatchId, member, memberActionContext);
		String targetName = memberActionContext.getTarget();

		if (memberPatchId != fieldTargetId
				&& targetName != null) {
			log(INFO, "Mapping member " + memberPatchId + " => " + targetName);
			targetMap.put(memberPatchId, targetName);
		}

		return targetMap;
	}

	private boolean shouldRetarget(PatcherAnnotation actionContext) {
		return actionContext.getAction() == Action.EDIT
				|| actionContext.getAction() == Action.REPLACE
				|| actionContext.getAction() == Action.WRAP
				|| actionContext.getAction() == Action.APPEND
				|| actionContext.getAction() == Action.PREPEND;
	}

	protected void log(Logger.Level level, String message) {
		logger.log(level, "Retargeter: " + message);
	}
}
