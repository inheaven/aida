package ru.inhell.aida.mybatis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 31.05.12 18:34
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface XmlMapper {
}
