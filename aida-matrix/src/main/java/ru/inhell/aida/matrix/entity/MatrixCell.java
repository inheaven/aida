package ru.inhell.aida.matrix.entity;

import ru.inhell.aida.common.entity.TransactionType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 05.06.12 17:28
 */
public class MatrixCell {
    private Map<Long, Matrix> matrixMap = new HashMap<>();

    public MatrixCell() {
    }

    public void add(Matrix matrix){
        matrixMap.put(matrix.getId(), matrix);
    }

    public int getBuyQuantity() {
        int buyQuantity = 0;

        for (Matrix matrix : matrixMap.values()){
            if (TransactionType.BUY.equals(matrix.getTransaction())){
                buyQuantity += matrix.getQuantity();
            }
        }

        return buyQuantity;
    }

    public int getSellQuantity() {
        int sellQuantity = 0;

        for (Matrix matrix : matrixMap.values()){
            if (TransactionType.SELL.equals(matrix.getTransaction())){
                sellQuantity += matrix.getQuantity();
            }
        }

        return sellQuantity;
    }

    public Collection<Matrix> values(){
        return matrixMap.values();
    }
}
