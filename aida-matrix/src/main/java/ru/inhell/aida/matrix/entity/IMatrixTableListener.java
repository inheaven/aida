package ru.inhell.aida.matrix.entity;

import java.io.Serializable;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 03.08.12 20:00
 */
public interface IMatrixTableListener extends Serializable {
    void onChange(MatrixEvent event);
}
