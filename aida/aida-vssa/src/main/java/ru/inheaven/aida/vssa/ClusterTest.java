package ru.inheaven.aida.vssa;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientUserCodeDeploymentConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.impl.DataAwareMessage;

import java.util.concurrent.CountDownLatch;

/**
 * @author Anatoly A. Ivanov
 * 17.02.2018 12:58
 */
public class ClusterTest {

    public static void main(String[] args) throws InterruptedException {
        System.setProperty("hazelcast.diagnostics.enabled", "true");

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getGroupConfig().setName("dev");
        clientConfig.getNetworkConfig().addAddress("192.168.0.16:32463");

        ClientUserCodeDeploymentConfig clientUserCodeDeploymentConfig = new ClientUserCodeDeploymentConfig();
        clientUserCodeDeploymentConfig.addClass("ru.aida.vssa.EchoTask");
        clientUserCodeDeploymentConfig.setEnabled(true);
        clientConfig.setUserCodeDeploymentConfig(clientUserCodeDeploymentConfig);

        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);

        client.getExecutorService("executor").submitToAllMembers(new EchoTask());

        client.getTopic("topic").<DataAwareMessage>addMessageListener(m -> System.out.println(
                m.getPublishingMember() + " " + m.getMessageObject()));

        new CountDownLatch(1).await();
    }
}
