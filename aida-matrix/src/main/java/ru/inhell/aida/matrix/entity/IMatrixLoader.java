package ru.inhell.aida.matrix.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 02.08.12 18:13
 */
public interface IMatrixLoader extends Serializable {
    List<Matrix> load(MatrixType matrixType, Date start, Date end);
}
