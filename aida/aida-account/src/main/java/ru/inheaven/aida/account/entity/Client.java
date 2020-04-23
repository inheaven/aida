package ru.inheaven.aida.account.entity;

import ru.inheaven.aida.common.entity.AbstractEntity;

/**
 * inheaven on 19.04.2016.
 */
public class Client extends AbstractEntity {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
