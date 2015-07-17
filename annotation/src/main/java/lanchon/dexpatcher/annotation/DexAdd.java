package lanchon.dexpatcher.annotation;

import java.lang.annotation.*;

@DexIgnore
@Documented
@Target({ ElementType.PACKAGE, ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD })
public @interface DexAdd {}
