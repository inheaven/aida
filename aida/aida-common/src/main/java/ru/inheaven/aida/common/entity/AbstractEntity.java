package ru.inheaven.aida.common.entity;

import java.io.Serializable;

/**
 * inheaven on 19.04.2016.
 */
public abstract class AbstractEntity implements Serializable{
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
