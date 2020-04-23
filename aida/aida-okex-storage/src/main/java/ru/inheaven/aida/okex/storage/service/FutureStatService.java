package ru.inheaven.aida.okex.storage.service;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Anatoly A. Ivanov
 * 22.09.2017 18:59
 */
public class FutureStatService {
    private Logger log = LoggerFactory.getLogger(FutureStatService.class);

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";

    @Inject
    private ClusterService clusterService;

    private Random random = new SecureRandom();

    public FutureStatService() {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() ->{
            try {
                URL eliteScaleBtc = new URL("https://www.okex.com/v2/futures/pc/public/eliteScale.do?symbol=0&type=0&random=" + random.nextInt(100));
                URL eliteScaleLtc = new URL("https://www.okex.com/v2/futures/pc/public/eliteScale.do?symbol=1&type=0&random=" + random.nextInt(100));

                URL futurePositionRatioBtc = new URL("https://www.okex.com/v2/futures/pc/public/getFuturePositionRatio.do?symbol=0&type=0&random=" + random.nextInt(100));
                URL futurePositionRatioLtc = new URL("https://www.okex.com/v2/futures/pc/public/getFuturePositionRatio.do?symbol=1&type=0&random=" + random.nextInt(100));

                insert("elite_scale", "btc", getString(eliteScaleBtc));
                insert("elite_scale", "ltc", getString(eliteScaleLtc));

                insert("future_position", "btc", getString(futurePositionRatioBtc));
                insert("future_position", "ltc", getString(futurePositionRatioLtc));
            } catch (Throwable t) {
                log.error("error scheduler", t);
            }
        },0, 10, TimeUnit.MINUTES);

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() ->{
            try {
                URL futureVolumeBtc = new URL("https://www.okex.com/v2/futures/pc/public/futureVolume.do?symbol=0&typesum=1&_=" + (System.currentTimeMillis()));
                URL futureVolumeLtc = new URL("https://www.okex.com/v2/futures/pc/public/futureVolume.do?symbol=1&typesum=1&_=" + (System.currentTimeMillis()));

                insert("future_volume", "btc", getString(futureVolumeBtc));
                insert("future_volume", "ltc", getString(futureVolumeLtc));
            } catch (Throwable t) {
                log.error("error scheduler", t);
            }
        },0, 1, TimeUnit.HOURS);
    }

    private String getString(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", USER_AGENT);

        String s = null;

        try(InputStreamReader reader = new InputStreamReader(connection.getInputStream(), Charsets.UTF_8)) {
            s = CharStreams.toString(reader);
        }

        return s;
    }

    private void insert(String table, String symbol, String message){
        try {
            clusterService.getSession().executeAsync(QueryBuilder.insertInto("okex", table)
                    .value("id", System.currentTimeMillis())
                    .value("symbol", symbol)
                    .value("message", message));
        } catch (Exception e) {
            log.error("error cluster", e);
        }
    }
}
