package ru.inheaven.aida.happy.trading.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author inheaven on 02.12.2016.
 */
public class ExperienceTest {
    class Experience {
        long t;
        String e;
        long count;
    }

    Map<String, Experience> map = new HashMap<>();

    void addExperience(long t, String e){
        if (map.get(e) != null){
            map.get(e).count++;
        }else{
            Experience experience = new Experience();

            experience.t = t;
            experience.e = e;
            experience.count = 1;

            map.put(e, experience);
        }
    }

    long isBelief(String e){
        return map.get(e) != null ? map.get(e).count : 0;
    }

    public static void main(String[] args) {

    }
}
