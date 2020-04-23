package ru.inheaven.aida.vssa;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * @author Anatoly A. Ivanov
 * 17.02.2018 18:24
 */
public class EchoTask implements Callable<Integer>, Serializable, HazelcastInstanceAware {

    private transient HazelcastInstance hz;

    public void setHazelcastInstance(HazelcastInstance hz) {
        this.hz = hz;
    }

    public Integer call() throws Exception{
        for (int i = 0; i < 3600; i++) {

            hz.getMap("strings").put(i, i);

            hz.getTopic("topic").publish("hello topic" + i);

            System.out.println("Echo: " + Thread.currentThread().getName() + " " + i);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return Math.toIntExact(System.currentTimeMillis());
    }
}
