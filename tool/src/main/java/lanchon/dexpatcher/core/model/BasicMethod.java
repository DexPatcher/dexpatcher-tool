package lanchon.dexpatcher.core.model;

import java.util.List;
import java.util.Set;

import org.jf.dexlib2.base.reference.BaseMethodReference;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;

public class BasicMethod extends BaseMethodReference implements Method {

	private final String definingClass;
	private final String name;
	private final List<? extends MethodParameter> parameters;
	private final String returnType;
	private final int accessFlags;
	private final Set<? extends Annotation> annotations;
	private final MethodImplementation methodImplementation;

	public BasicMethod(
			String definingClass,
			String name,
			List<? extends MethodParameter> parameters,
			String returnType,
			int accessFlags,
			Set<? extends Annotation> annotations,
			MethodImplementation methodImplementation
	) {
		this.definingClass = definingClass;
		this.name = name;
		this.parameters = parameters;
		this.returnType = returnType;
		this.accessFlags = accessFlags;
		this.annotations = annotations;
		this.methodImplementation = methodImplementation;
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
	public List<? extends MethodParameter> getParameters() {
		return parameters;
	}

	@Override
	public String getReturnType() {
		return returnType;
	}

	@Override
	public int getAccessFlags() {
		return accessFlags;
	}

	@Override
	public Set<? extends Annotation> getAnnotations() {
		return annotations;
	}

	@Override
	public MethodImplementation getImplementation() {
		return methodImplementation;
	}

	@Override
	public List<? extends CharSequence> getParameterTypes() {
		return parameters;
	}

}
