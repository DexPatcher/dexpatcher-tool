package lanchon.dexpatcher.annotation;

import java.lang.annotation.*;

@DexIgnore
@Documented
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD})
public @interface DexReplace {
    String target() default "";
	Class<?> targetClass() default Void.class;
}
