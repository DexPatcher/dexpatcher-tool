package lanchon.dexpatcher.annotation;

import java.lang.annotation.*;

@DexIgnore
@Documented
@Target({ ElementType.PACKAGE, ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD })
public @interface DexReplace {
	String target() default "";
	Class<?> targetClass() default Void.class;
	boolean recursive() default false;
}
