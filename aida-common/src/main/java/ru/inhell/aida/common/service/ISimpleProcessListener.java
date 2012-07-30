package ru.inhell.aida.common.service;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 30.07.12 16:28
 */
public interface ISimpleProcessListener<T> {
    void processed(T o);
}
