/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.mapper;

import org.jf.dexlib2.DebugItemType;
import org.jf.dexlib2.ValueType;
import org.jf.dexlib2.base.reference.BaseStringReference;
import org.jf.dexlib2.base.value.BaseStringEncodedValue;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.debug.DebugItem;
import org.jf.dexlib2.iface.debug.EndLocal;
import org.jf.dexlib2.iface.debug.RestartLocal;
import org.jf.dexlib2.iface.debug.StartLocal;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.iface.value.StringEncodedValue;
import org.jf.dexlib2.rewriter.AnnotationElementRewriter;
import org.jf.dexlib2.rewriter.DebugItemRewriter;
import org.jf.dexlib2.rewriter.FieldReferenceRewriter;
import org.jf.dexlib2.rewriter.MethodParameterRewriter;
import org.jf.dexlib2.rewriter.MethodReferenceRewriter;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.Rewriters;

public abstract class DexDecoderModule extends RewriterModule {

	public abstract String rewriteItem(String definingClass, String type, String value);

	@Override
	public Rewriter<String> getTypeRewriter(Rewriters rewriters) {
		return new NakedReferenceTypeRewriter() {
			@Override
			public String rewriteNakedReferenceType(String nakedReferenceType) {
				return rewriteTypeName(nakedReferenceType);
			}
		};
	}

	public final String rewriteTypeName(String nakedReferenceType) {
		return rewriteItem(null, "type", nakedReferenceType);
	}

	@Override
	public Rewriter<FieldReference> getFieldReferenceRewriter(Rewriters rewriters) {
		return new FieldReferenceRewriter(rewriters) {
			@Override
			public FieldReference rewrite(FieldReference fieldReference) {
				return new RewrittenFieldReference(fieldReference) {
					@Override
					public String getName() {
						return rewriteFieldName(fieldReference.getDefiningClass(), fieldReference.getName());
					}
				};
			}
		};
	}

	public final String rewriteFieldName(String definingClass, String value) {
		return rewriteItem(definingClass, "field", value);
	}

	@Override
	public Rewriter<MethodReference> getMethodReferenceRewriter(Rewriters rewriters) {
		return new MethodReferenceRewriter(rewriters) {
			@Override
			public MethodReference rewrite(MethodReference methodReference) {
				return new RewrittenMethodReference(methodReference) {
					@Override
					public String getName() {
						return rewriteMethodName(methodReference.getDefiningClass(), methodReference.getName());
					}
				};
			}
		};
	}

	public final String rewriteMethodName(String definingClass, String value) {
		return rewriteItem(definingClass, "method", value);
	}

	@Override
	public Rewriter<MethodParameter> getMethodParameterRewriter(Rewriters rewriters) {
		return new MethodParameterRewriter(rewriters) {
			@Override
			public MethodParameter rewrite(MethodParameter methodParameter) {
				return new RewrittenMethodParameter(methodParameter) {
					@Override
					public String getName() {
						return rewriteParameterName(methodParameter.getName());
					}
				};
			}

		};
	}

	public final String rewriteParameterName(String value) {
		return rewriteItem(null, "parameter", value);
	}

	@Override
	public Rewriter<DebugItem> getDebugItemRewriter(Rewriters rewriters) {
		return new DebugItemRewriter(rewriters) {
			@Override
			public DebugItem rewrite(DebugItem value) {
				switch (value.getDebugItemType()) {
					case DebugItemType.START_LOCAL:
						return new RewrittenStartLocal((StartLocal) value) {
							@Override
							public String getName() {
								return rewriteLocalVariableName(debugItem.getName());
							}
							@Override
							public StringReference getNameReference() {
								final StringReference ref = debugItem.getNameReference();
								if (ref == null) return null;
								return new BaseStringReference() {
									@Override public String getString() {
										return rewriteLocalVariableName(ref.getString());
									}
								};

							}
						};
					case DebugItemType.END_LOCAL:
						return new RewrittenEndLocal((EndLocal) value) {
							@Override
							public String getName() {
								return rewriteLocalVariableName(debugItem.getName());
							}
						};
					case DebugItemType.RESTART_LOCAL:
						return new RewrittenRestartLocal((RestartLocal) value) {
							@Override
							public String getName() {
								return rewriteLocalVariableName(debugItem.getName());
							}
						};
					default:
						return super.rewrite(value);
				}
			}
		};
	}

	public final String rewriteLocalVariableName(String value) {
		return rewriteItem(null, "local variable", value);
	}

	@Override
	public Rewriter<AnnotationElement> getAnnotationElementRewriter(Rewriters rewriters) {
		return new AnnotationElementRewriter(rewriters) {
			@Override
			public AnnotationElement rewrite(AnnotationElement annotationElement) {
				return new RewrittenAnnotationElement(annotationElement) {
					@Override
					public String getName() {
						return rewriteAnnotationElementName(annotationElement.getName());
					}
					@Override
					public EncodedValue getValue() {
						EncodedValue value = annotationElement.getValue();
						switch (value.getValueType()) {
							case ValueType.STRING:
								final StringEncodedValue stringValue = (StringEncodedValue) value;
								return new BaseStringEncodedValue() {
									@Override
									public String getValue() {
										return rewriteAnnotationElementStringValue(stringValue.getValue());
									}
								};
							default:
								return super.getValue();
						}
					}
				};
			}
		};
	}

	public final String rewriteAnnotationElementName(String value) {
		return rewriteItem(null, "annotation element name", value);
	}

	public final String rewriteAnnotationElementStringValue(String value) {
		return rewriteItem(null, "annotation element value", value);
	}

}
