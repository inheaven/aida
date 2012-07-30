package ru.inhell.aida.common.service;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 05.06.12 15:33
 */
public interface IProcessListener<T>  extends ISimpleProcessListener<T>{
    void skipped(T o);
    void error(T o, Exception e);
}
