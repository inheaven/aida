package ru.inheaven.aida.happy.trading.util;

import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * inheaven on 05.03.2016.
 */
public class BibleRandom {
    private static BibleRandom INSTANCE = new BibleRandom();

    private List<Double> bibleRandomList = new ArrayList<>();
    private AtomicLong index = new AtomicLong(0);

    private BibleRandom() {
        try {
            Path path = FileSystems.getDefault().getPath("/opt/data", "kjvdat.txt");
            List<String> quranList = Files.readAllLines(path, Charset.forName("UTF-8"));

            for (String q : quranList) {
                char[] chars = q.substring(q.indexOf(' ')).toCharArray();

                for (char c : chars) {
                    c = Character.toLowerCase(c);

                    if (Character.isLetter(c)) {
                        bibleRandomList.add((double) (123 - (int) c) / 26);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double nextDouble(){
        return INSTANCE.bibleRandomList.get((int) (INSTANCE.index.incrementAndGet() % INSTANCE.bibleRandomList.size()));
    }

    public static Long getIndex(){
        return INSTANCE.index.get();
    }

    public static void main(String[] args){

    }

}
