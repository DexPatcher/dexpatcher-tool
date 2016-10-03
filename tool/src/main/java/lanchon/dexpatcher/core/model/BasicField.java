package lanchon.dexpatcher.core.model;

import java.util.Set;

import org.jf.dexlib2.base.reference.BaseFieldReference;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.value.EncodedValue;

public class BasicField extends BaseFieldReference implements Field {

	private final String definingClass;
	private final String name;
	private final String type;
	private final int accessFlags;
	private final EncodedValue initialValue;
	private final Set<? extends Annotation> annotations;

	public BasicField(
			String definingClass,
			String name,
			String type,
			int accessFlags,
			EncodedValue initialValue,
			Set<? extends Annotation> annotations
	) {
		this.definingClass = definingClass;
		this.name = name;
		this.type = type;
		this.accessFlags = accessFlags;
		this.initialValue = initialValue;
		this.annotations = annotations;
	}

	@Override
	public String getDefiningClass() {
		return definingClass;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public int getAccessFlags() {
		return accessFlags;
	}

	@Override
	public EncodedValue getInitialValue() {
		return initialValue;
	}

	@Override
	public Set<? extends Annotation> getAnnotations() {
		return annotations;
	}

}
