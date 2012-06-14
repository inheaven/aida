package ru.inhell.aida.common.web;

import org.apache.wicket.ajax.AjaxRequestTarget;

import java.io.Serializable;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 14.06.12 21:27
 */
public interface IUpdateListener extends Serializable {
    void onUpdate(AjaxRequestTarget target);
}
