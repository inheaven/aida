package ru.inhell.aida.level.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 12.12.12 16:21
 */
@Entity
@Table
public class Level implements Serializable {
    @Id
    private Long id;

    @Column
    private int index;

    @Column
    private int planLot;

    @Column
    private int activeLot;

    @Column
    private float buyPrice;

    @Column
    private float sellPrice;

    @ManyToOne
    @JoinColumn(name = "stock_id")
    private Stock stock;

    public Level() {
    }

    public Level(int index) {
        this.index = index;
    }

    public Level(int index, int planLot, float buyPrice, float sellPrice) {
        this.index = index;
        this.planLot = planLot;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getPlanLot() {
        return planLot;
    }

    public void setPlanLot(int planLot) {
        this.planLot = planLot;
    }

    public int getActiveLot() {
        return activeLot;
    }

    public void setActiveLot(int activeLot) {
        this.activeLot = activeLot;
    }

    public float getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(float buyPrice) {
        this.buyPrice = buyPrice;
    }

    public float getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(float sellPrice) {
        this.sellPrice = sellPrice;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }
}
