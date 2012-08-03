package ru.inhell.aida.matrix.entity;

import java.io.Serializable;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 04.08.12 1:00
 */
public class MatrixEvent implements Serializable {
    private List<MatrixCell> matrixCells;
    private boolean cropped;

    public MatrixEvent(List<MatrixCell> matrixCells, boolean cropped) {
        this.matrixCells = matrixCells;
        this.cropped = cropped;
    }

    public List<MatrixCell> getMatrixCells() {
        return matrixCells;
    }

    public void setMatrixCells(List<MatrixCell> matrixCells) {
        this.matrixCells = matrixCells;
    }

    public boolean isCropped() {
        return cropped;
    }

    public void setCropped(boolean cropped) {
        this.cropped = cropped;
    }
}
