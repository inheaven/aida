package ru.inheaven.aida.coin.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 07.01.14 19:42
 */
@MappedSuperclass
public abstract class AbstractEntity implements Serializable {
    public AbstractEntity() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
