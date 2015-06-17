package ru.inhell.aida.common.wicket;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;

import java.util.function.Supplier;

/**
 * @author inheaven on 017 17.06.15 18:51
 */
public class AjaxLabel<T> extends Label {
    public AjaxLabel(String id, Supplier<T> label) {
        super(id, new LoadableDetachableModel<T>() {
            @Override
            protected T load() {
                return label.get();
            }
        });
        setOutputMarkupId(true);
    }
}
