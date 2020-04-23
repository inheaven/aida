package ru.inheaven.aida.wallet;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.Wallet;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * @author Anatoly A. Ivanov
 *         Date: 13.03.2017.
 */
public class WalletImportService {
    private static OptionSet options;
    private static OptionSpec<String> keys;
    private static OptionSpec<String> blocks;
    private static OptionSpec<Integer> threads;
    private static OptionSpec<Integer> skip;
    private static OptionSpec<String> result;

    private static List<String> gdata;

    private static Path resultPath;

    public static void main(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        keys = parser.accepts("keys", "<file>").withRequiredArg().required();
        blocks = parser.accepts("blocks", "<dir>").withRequiredArg().required();
        threads = parser.accepts("threads", "N").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors()-1);
        skip = parser.accepts("skip", "skip N blocks").withRequiredArg().ofType(Integer.class).defaultsTo(0);
        result = parser.accepts("result", "<file>").withRequiredArg().required();

        try {
            options = parser.parse(args);
        } catch (Exception e) {
            parser.printHelpOn(System.out);
            return;
        }

        try {
            load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void test() throws BlockStoreException, ExecutionException, PrunedException, InterruptedException, IOException {
        gdata = new ArrayList<>();
        gdata.add("L2teCUhg4Ww9r5aLpyY4qVtQify3eeciwD3EPyGeRopKny8N7d5o");
        load(gdata);
    }

    private static void load() throws IOException{
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(threads.value(options)));

        resultPath = FileSystems.getDefault().getPath(result.value(options));
        if (!Files.isWritable(resultPath)) {
            Files.createFile(resultPath);
        }

        gdata = Files.readAllLines(FileSystems.getDefault().getPath(keys.value(options)));

        log("Read " + gdata.size() + " keys");

        try {
            load(gdata);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void load(List<String> data) throws BlockStoreException, PrunedException, ExecutionException, InterruptedException, IOException {
        BriefLogFormatter.initWithSilentBitcoinJ();
        Threading.ignoreLockCycles();

        NetworkParameters params = MainNetParams.get();

        //wallet
        List<Wallet> wallets = new ArrayList<>();
        data.parallelStream().forEach(d -> {
            Wallet wallet = new Wallet(params);

            ECKey key = DumpedPrivateKey.fromBase58(params, d).getKey();
            key.setCreationTimeSeconds(0);

            wallet.importKey(key);

//            log(key.toAddress(params) + " " + key);

            wallets.add(wallet);
        });

        log("Init " + wallets.size() + " wallets");

        wallets.forEach(wallet -> {
            wallet.addCoinsReceivedEventListener((w, tx, prevBalance, newBalance) ->
                    log(w.getLastBlockSeenTime() + " " + newBalance.toFriendlyString() + " " +
                            w.getImportedKeys().get(0).toAddress(params) + " " + gdata.indexOf(data.get(wallets.indexOf(w)))));
            wallet.addCoinsSentEventListener((w, tx, prevBalance, newBalance) ->
                    log(w.getLastBlockSeenTime() + " " + newBalance.toFriendlyString() + " " +
                            w.getImportedKeys().get(0).toAddress(params) + " " + gdata.indexOf(data.get(wallets.indexOf(w)))));
        });

        MemoryBlockStore blockStore = new MemoryBlockStore(params);

        BlockChain chain = new BlockChain(params, blockStore);

        //load chain
        List<File> blockChainFiles = new LinkedList<>();

        for (int i = 0; i < 10000; i++) {
            File file = new File(blocks.value(options), String.format(Locale.US, "blk%05d.dat", i));

            if (file.exists()){
                blockChainFiles.add(file);
            }
        }

        BlockFilePreLoader loader = new BlockFilePreLoader(params, blockChainFiles);

        int index = 0;
        int s = skip.value(options);

        for (; chain.getBestChainHeight() <= s; index++){
            Block block = loader.next();

            try {
                chain.add(block);
            } catch (Exception e) {
                log(e.getLocalizedMessage());
            }

            if (index++%10000 == 0){
                log(block.getTime() + " " + chain.getBestChainHeight() + " " + (index-1));
            }
        }

        wallets.forEach(w -> {
            w.setLastBlockSeenHeight(chain.getBestChainHeight());
            chain.addWallet(w);
        });

        for (; loader.hasNext(); index++){
            Block block = loader.next();

            try {
                if (block.getTimeSeconds() - chain.getChainHead().getHeader().getTimeSeconds() > -86400) {
                    chain.add(block);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (index++%10000 == 0){
                log(block.getTime() + " " + chain.getBestChainHeight() + " " + (index-1));
            }
        }

        wallets.sort(Comparator.comparingLong(w -> w.getBalance().getValue()));

        wallets.forEach(w -> log(w.getLastBlockSeenTime() + " " + w.getLastBlockSeenHeight() + " " +
                w.getBalance().toFriendlyString() + " " + w.getImportedKeys().get(0).toAddress(params)));
    }

    private static void log(String s){
        s +=  "\n";

        System.out.print(s);

        try {
            Files.write(resultPath, s.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
