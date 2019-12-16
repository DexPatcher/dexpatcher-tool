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

import java.util.List;
import java.util.Set;

import lanchon.dexpatcher.core.Action;
import lanchon.dexpatcher.core.ActionParser;
import lanchon.dexpatcher.core.Marker;
import lanchon.dexpatcher.core.util.DexUtils;
import lanchon.dexpatcher.core.util.Target;

import org.jf.dexlib2.base.reference.BaseFieldReference;
import org.jf.dexlib2.base.reference.BaseMethodReference;
import org.jf.dexlib2.base.value.BaseStringEncodedValue;
import org.jf.dexlib2.iface.Annotatable;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.iface.value.StringEncodedValue;
import org.jf.dexlib2.rewriter.AnnotationElementRewriter;
import org.jf.dexlib2.rewriter.AnnotationRewriter;
import org.jf.dexlib2.rewriter.ClassDefRewriter;
import org.jf.dexlib2.rewriter.FieldRewriter;
import org.jf.dexlib2.rewriter.MethodRewriter;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.RewriterUtils;
import org.jf.dexlib2.rewriter.Rewriters;

public abstract class PatchRewriterModule extends RewriterModule {

	protected final ActionParser actionParser;

	public PatchRewriterModule(ActionParser actionParser) {
		this.actionParser = actionParser;
	}

	@Override
	public Rewriter<ClassDef> getClassDefRewriter(Rewriters rewriters) {
		return new ClassDefRewriter(rewriters) {
			@Override
			public ClassDef rewrite(ClassDef classDef) {
				return new RewrittenClassDef(classDef) {
					@Override
					public Set<? extends Annotation> getAnnotations() {
						return getRewrittenAnnotations(rewriters, classDef, CLASS_ANNOTATION_REWRITER);
					}
				};
			}
		};
	}

	@Override
	public Rewriter<Field> getFieldRewriter(Rewriters rewriters) {
		return new FieldRewriter(rewriters) {
			@Override
			public Field rewrite(Field field) {
				return new RewrittenField(field) {
					@Override
					public Set<? extends Annotation> getAnnotations() {
						return getRewrittenAnnotations(rewriters, field, FIELD_ANNOTATION_REWRITER);
					}
				};
			}
		};
	}

	@Override
	public Rewriter<Method> getMethodRewriter(Rewriters rewriters) {
		return new MethodRewriter(rewriters) {
			@Override
			public Method rewrite(Method method) {
				return new RewrittenMethod(method) {
					@Override
					public Set<? extends Annotation> getAnnotations() {
						return getRewrittenAnnotations(rewriters, method, METHOD_ANNOTATION_REWRITER);
					}
				};
			}
		};
	}

	protected interface AnnotationElementValueRewriter<I extends Annotatable> {
		EncodedValue rewrite(Rewriters rewriters, I item, Action action, String elementName, EncodedValue elementValue);
	}

	public <I extends Annotatable> Set<? extends Annotation> getRewrittenAnnotations(Rewriters rewriters, final I item,
			final AnnotationElementValueRewriter<I> annotationElementValueRewriter) {
		Set<? extends Annotation> annotations = item.getAnnotations();
		Rewriter<Annotation> annotationRewriter = annotations.isEmpty() ?
				rewriters.getAnnotationRewriter() : new AnnotationRewriter(rewriters) {
			@Override
			public Annotation rewrite(Annotation value) {
				return new RewrittenAnnotation(value) {
					@Override
					public Set<? extends AnnotationElement> getElements() {
						final Action action = actionParser.getActionFromTypeDescriptor(annotation.getType());
						if (action == null) return super.getElements();
						AnnotationElementRewriter annotationElementRewriter = new AnnotationElementRewriter(rewriters) {
							@Override
							public AnnotationElement rewrite(AnnotationElement annotationElement) {
								return new RewrittenAnnotationElement(annotationElement) {
									@Override
									public EncodedValue getValue() {
										return annotationElementValueRewriter.rewrite(rewriters, item, action,
												annotationElement.getName(), annotationElement.getValue());
									}
								};
							}
						};
						return RewriterUtils.rewriteSet(annotationElementRewriter, annotation.getElements());
					}
				};
			}
		};
		return RewriterUtils.rewriteSet(annotationRewriter, annotations);
	}

	public static final AnnotationElementValueRewriter<ClassDef> CLASS_ANNOTATION_REWRITER =
			new AnnotationElementValueRewriter<ClassDef>() {
				@Override
				public EncodedValue rewrite(final Rewriters rewriters, final ClassDef classDef, Action action,
						String elementName, EncodedValue elementValue) {
					if (Marker.ELEM_TARGET.equals(elementName)) {
						final String target = ((StringEncodedValue) elementValue).getValue();
						if (target.length() != 0) {
							return new BaseStringEncodedValue() {
								@Override
								public String getValue() {
									String baseDescriptor = classDef.getType();
									String targetDescriptor = DexUtils.isPackageDescriptor(baseDescriptor) ?
											Target.resolvePackageDescriptor(target) :
											Target.resolveClassDescriptor(baseDescriptor, target);
									return rewriters.getTypeRewriter().rewrite(targetDescriptor);
								}
							};
						}
					}
					return rewriters.getEncodedValueRewriter().rewrite(elementValue);
				}
			};

	public static final AnnotationElementValueRewriter<Field> FIELD_ANNOTATION_REWRITER =
			new AnnotationElementValueRewriter<Field>() {
				@Override
				public EncodedValue rewrite(final Rewriters rewriters, final Field field, Action action,
						String elementName, EncodedValue elementValue) {
					if (Marker.ELEM_TARGET.equals(elementName)) {
						final String target = ((StringEncodedValue) elementValue).getValue();
						if (target.length() != 0) {
							return new BaseStringEncodedValue() {
								@Override
								public String getValue() {
									return rewriters.getFieldReferenceRewriter().rewrite(new BaseFieldReference() {
										@Override
										public String getDefiningClass() {
											return field.getDefiningClass();
										}
										@Override
										public String getType() {
											return field.getType();
										}
										@Override
										public String getName() {
											return target;
										}
									}).getName();
								}
							};
						}
					}
					return rewriters.getEncodedValueRewriter().rewrite(elementValue);
				}
			};

	public static final AnnotationElementValueRewriter<Method> METHOD_ANNOTATION_REWRITER =
			new AnnotationElementValueRewriter<Method>() {
				@Override
				public EncodedValue rewrite(final Rewriters rewriters, final Method method, Action action,
						String elementName, EncodedValue elementValue) {
					if (Marker.ELEM_TARGET.equals(elementName)) {
						final String target = ((StringEncodedValue) elementValue).getValue();
						if (target.length() != 0) {
							return new BaseStringEncodedValue() {
								@Override
								public String getValue() {
									return rewriters.getMethodReferenceRewriter().rewrite(new BaseMethodReference() {
										@Override
										public String getDefiningClass() {
											return method.getDefiningClass();
										}
										@Override
										public List<? extends CharSequence> getParameterTypes() {
											return method.getParameterTypes();
										}
										@Override
										public String getReturnType() {
											return method.getReturnType();
										}
										@Override
										public String getName() {
											return target;
										}
									}).getName();
								}
							};
						}
					}
					return rewriters.getEncodedValueRewriter().rewrite(elementValue);
				}
			};

}
