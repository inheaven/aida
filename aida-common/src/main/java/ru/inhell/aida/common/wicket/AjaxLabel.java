package ru.inhell.aida.common.wicket;

import org.apache.wicket.markup.html.basic.Label;

import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * @author inheaven on 017 17.06.15 18:51
 */
public class AjaxLabel<T> extends Label {
    public AjaxLabel(String id, Function<Object, Object>) {
        super(id);

    }
}
