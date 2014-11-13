package ru.inheaven.aida.coin.service;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSinkImages;
import ru.inheaven.aida.coin.entity.Trader;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        traders.forEach(trader -> {
            wallets.add(trader.getCurrency());
            wallets.add(trader.getCounterSymbol());
        });

        Graph graph = new MultiGraph("graph");

        wallets.forEach(graph::addNode);

        for (Trader trader : traders){
            String id = trader.getExchange().name() + "/" + trader.getPair();
            graph.addEdge(id + "/ASK", trader.getCurrency(), trader.getCounterSymbol());
            graph.addEdge(id + "/BID", trader.getCounterSymbol(), trader.getCurrency());
        }

        FileSinkImages pic = new FileSinkImages(FileSinkImages.OutputType.PNG, FileSinkImages.Resolutions.VGA);
        pic.writeAll(graph, "graph");

        return null;
    }
}
