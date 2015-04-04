package lanchon.dexpatcher.annotation;

import java.lang.annotation.*;

@DexIgnore
@Documented
@Target({ ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD })
public @interface DexEdit {
	String target() default "";
	Class<?> targetClass() default Void.class;
	DexAction staticConstructorAction() default DexAction.UNDEFINED;
	DexAction defaultAction() default DexAction.UNDEFINED;
	boolean onlyEditMembers() default false;
}
