package ru.inheaven.aida.happy.trading.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * inheaven on 22.02.2016.
 */
public class QuranRandom {
    private static QuranRandom INSTANCE = new QuranRandom();

    private List<Double> quranRandomList = new ArrayList<>();
    private AtomicLong index = new AtomicLong(0);

    private QuranRandom() {
        try {
            Path path = FileSystems.getDefault().getPath("/opt/data", "quran-utf8.txt");
            List<String> quranList = Files.readAllLines(path, Charset.forName("UTF-8"));

            List<Character> list = new ArrayList<>();

            for (String q : quranList) {
                char[] chars = q.toCharArray();

                for (char c : chars) {
                    if (Character.isAlphabetic(c) && !list.contains(c)) {
                        list.add(c);
                    }
                }
            }

            list.sort(Comparator.reverseOrder());

            for (String q : quranList) {
                char[] chars = q.toCharArray();

                for (char c : chars) {
                    if (Character.isLetter(c)) {
                        quranRandomList.add((double) (39 - list.indexOf(c)) / 39);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double nextDouble(){
        return INSTANCE.quranRandomList.get((int) (INSTANCE.index.incrementAndGet() % INSTANCE.quranRandomList.size()));
    }

    public static void main(String[] args){

    }
}
