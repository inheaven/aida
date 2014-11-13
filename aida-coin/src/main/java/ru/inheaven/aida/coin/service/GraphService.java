package ru.inheaven.aida.coin.service;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkImages;
import ru.inheaven.aida.coin.entity.Trader;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * inheaven on 13.11.2014 21:14.
 */
@Stateless
public class GraphService {
    @EJB
    private TraderService traderService;

    @EJB
    private TraderBean traderBean;

    public BufferedImage getGraph() throws IOException {
        List<Trader> traders = traderBean.getTraders();

        Set<String> wallets = new HashSet<>();
        traders.forEach(trader -> wallets.add(trader.getCurrency()));

        Graph graph = new SingleGraph("graph");

        wallets.forEach(graph::addNode);

        for (Trader trader : traders){
            graph.addEdge(trader.getPair(), trader.getCurrency(), trader.getCounterSymbol());
            graph.addEdge(trader.getPair(), trader.getCounterSymbol(), trader.getCurrency());
        }

        FileSinkImages pic = new FileSinkImages(FileSinkImages.OutputType.PNG, FileSinkImages.Resolutions.VGA);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        pic.writeAll(graph, output);

        byte[] data = output.toByteArray();
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        return ImageIO.read(input);
    }
}
