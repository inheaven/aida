package ru.inheaven.aida.stat.bitcoin;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.CheckpointManager;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.utils.BriefLogFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.stat.service.InfluxService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

/**
 * @author inheaven on 17.12.2016.
 */
@Singleton
public class BlockService {
    private Logger log = LoggerFactory.getLogger(BlockService.class);

    @Inject
    public BlockService(InfluxService influxService) {
        try {
            BriefLogFormatter.init();

            NetworkParameters params = MainNetParams.get();

            BlockStore blockStore = new SPVBlockStore(params, new File("/opt/data/blockchin.spv"));

            CheckpointManager.checkpoint(params, CheckpointManager.openStream(params), blockStore, System.currentTimeMillis());

            BlockChain chain = new BlockChain(params, blockStore);

            PeerGroup peerGroup = new PeerGroup(params, chain);
            peerGroup.addPeerDiscovery(new DnsDiscovery(params));

            peerGroup.addBlocksDownloadedEventListener((peer, block, filteredBlock, blocksLeft) -> {
                try {
                    influxService.addBlock((int) (peer.getBestHeight() - blocksLeft), block);
                } catch (Exception e) {
                    log.error("error add block", e);
                }
            });

            peerGroup.start();

            peerGroup.waitForPeers(10).get();
            peerGroup.startBlockChainDownload(new DownloadProgressTracker());
        } catch (Exception e) {
            log.error("error block service", e);
        }

    }
}
