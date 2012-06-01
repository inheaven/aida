package ru.inhell.aida.matrix.web;

import org.apache.wicket.markup.html.panel.Panel;
import ru.inhell.aida.matrix.service.MatrixService;

import javax.ejb.EJB;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.06.12 16:54
 */
public class MatrixPanel extends Panel {
    @EJB
    private MatrixService matrixService;

    public MatrixPanel(String id) {
        super(id);



    }
}
