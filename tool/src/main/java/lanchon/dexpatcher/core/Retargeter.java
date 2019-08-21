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

import static lanchon.dexpatcher.core.logger.Logger.Level.*;

public class Retargeter {

	Targets targets;
	Context context;
	Logger logger;

	public Retargeter(Context context) {
		this.targets = new Targets();
		this.context = context;
		this.logger = context.getLogger();
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
