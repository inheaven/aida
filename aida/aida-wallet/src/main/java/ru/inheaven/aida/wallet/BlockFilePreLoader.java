package ru.inheaven.aida.wallet;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.utils.BlockFileLoader;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Anatoly A. Ivanov
 *         Date: 20.03.2017.
 */
public class BlockFilePreLoader implements Iterator<Block> {
    private BlockFileLoader blockFileLoader;

    private Queue<Block> queue = new ConcurrentLinkedQueue<>();

    private AtomicBoolean next = new AtomicBoolean(true);

    public BlockFilePreLoader(NetworkParameters params, List<File> files) {
        blockFileLoader = new BlockFileLoader(params, files);

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            while (blockFileLoader.hasNext() && queue.size() < 1024) {
                queue.add(blockFileLoader.next());
            }

            if (!blockFileLoader.hasNext()){
                next.set(false);
            }
        },0, 10, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean hasNext() {
        return next.get() || !queue.isEmpty();
    }

    @Override
    public Block next() {
        while (hasNext()){
            Block block = queue.poll();

            if (block != null){
                return block;
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
