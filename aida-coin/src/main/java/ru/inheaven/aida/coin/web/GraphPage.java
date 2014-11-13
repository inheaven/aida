package ru.inheaven.aida.coin.web;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import ru.inheaven.aida.coin.service.GraphService;

import javax.ejb.EJB;
import java.io.IOException;

/**
 * inheaven on 13.11.2014 23:59.
 */
public class GraphPage extends AbstractPage {
    @EJB
    private GraphService graphService;

    public GraphPage() throws IOException {
        BufferedDynamicImageResource resource = new BufferedDynamicImageResource();
        resource.setImage(graphService.getGraph());

        add(new Image("graph", resource));
    }
}
