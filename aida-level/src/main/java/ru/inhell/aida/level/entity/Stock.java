package ru.inhell.aida.level.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 12.12.12 16:15
 */
@Entity
@Table(name = "level_stock")
public class Stock implements Serializable{
    @Id
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Column
    private int lot;

    @Column
    private boolean buy;

    @Column
    private boolean active;

    @OneToMany
    private List<Level> levels;

    public Stock() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getLot() {
        return lot;
    }

    public void setLot(int lot) {
        this.lot = lot;
    }

    public boolean isBuy() {
        return buy;
    }

    public void setBuy(boolean buy) {
        this.buy = buy;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Level> getLevels() {
        return levels;
    }

    public void setLevels(List<Level> levels) {
        this.levels = levels;
    }
}
