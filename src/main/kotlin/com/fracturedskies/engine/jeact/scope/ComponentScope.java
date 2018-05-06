package com.fracturedskies.engine.jeact.scope;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Scope;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Scope
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ComponentScope {
  final class Literal extends AnnotationLiteral<ComponentScope> implements ComponentScope {
    public static final Literal INSTANCE = new Literal();
  }
}
