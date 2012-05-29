package ru.inhell.aida.template.web;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.05.12 17:31
 */
public class Menu {
    private int order;
    private String groupKey;
    private Class page;

    public Menu() {
    }

    public Menu(int order, String groupKey, Class page) {
        this.order = order;
        this.groupKey = groupKey;
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

    public Class getPage() {
        return page;
    }

    public void setPage(Class page) {
        this.page = page;
    }
}
