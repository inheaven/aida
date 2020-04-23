package ru.inheaven.aida.okex.storage.service;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Anatoly A. Ivanov
 * 22.09.2017 19:05
 */
@Singleton
public class ClusterService {
    private Cluster cluster;
    private Session session;

    @Inject
    public void init(){
        while (true) {
            try {
                cluster = Cluster.builder()
                        .addContactPoint("192.168.0.18")
                        .addContactPoint("192.168.0.19")
                        .build();

                cluster.init();

                session = cluster.connect();
                break;
            } catch (Exception e) {
                e.printStackTrace();

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            cluster.close();

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }

    public Session getSession() {
        return session;
    }
}
