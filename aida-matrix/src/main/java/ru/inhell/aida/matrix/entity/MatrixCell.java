package ru.inhell.aida.matrix.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 05.06.12 17:28
 */
public class MatrixCell {
    private int buyQuantity;
    private int sellQuantity;

    public MatrixCell(int buyQuantity, int sellQuantity) {
        this.buyQuantity = buyQuantity;
        this.sellQuantity = sellQuantity;
    }

    public void clear(){
        buyQuantity = 0;
        sellQuantity = 0;
    }

    public int getBuyQuantity() {
        return buyQuantity;
    }

    public void setBuyQuantity(int buyQuantity) {
        this.buyQuantity = buyQuantity;
    }

    public int getSellQuantity() {
        return sellQuantity;
    }

    public void setSellQuantity(int sellQuantity) {
        this.sellQuantity = sellQuantity;
    }
}
