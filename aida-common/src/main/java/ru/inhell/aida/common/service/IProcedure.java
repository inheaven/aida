package ru.inhell.aida.common.service;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 27.06.12 15:01
 */
public interface IProcedure<T> {
    void apply(T object);
}
