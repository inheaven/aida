package ru.inhell.aida.template.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.05.12 18:01
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TemplateMenu{
    int order() default 0;
    String groupKey();
}
