package ru.inhell.aida.template.web;

import org.apache.wicket.Page;

import java.io.Serializable;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.05.12 17:31
 */
public class Menu implements Serializable {
    private int order;
    private String groupKey;
    private String titleKey;
    private Class<? extends Page> page;

    public Menu() {
    }

    public Menu(int order, String groupKey, String titleKey, Class<? extends Page> page) {
        this.order = order;
        this.groupKey = groupKey;
        this.titleKey = titleKey;
        this.page = page;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    public Class<? extends Page> getPage() {
        return page;
    }

    public void setPage(Class<? extends Page> page) {
        this.page = page;
    }
}
