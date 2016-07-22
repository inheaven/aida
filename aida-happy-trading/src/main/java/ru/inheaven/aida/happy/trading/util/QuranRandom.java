package ru.inheaven.aida.happy.trading.util;

import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * inheaven on 22.02.2016.
 */
public class QuranRandom {
    private static List<Double> quranRandomList = new ArrayList<>();
    private static QuranRandom INSTANCE = new QuranRandom();

    private AtomicLong index = new AtomicLong(0);

    public QuranRandom() {
        if (quranRandomList.isEmpty()) {
            try {
                Path path = FileSystems.getDefault().getPath("/opt/data", "quran-utf8.txt");
                List<String> quranList = Files.readAllLines(path, Charset.forName("UTF-8"));

                List<Character> list = new ArrayList<>();

                for (String q : quranList) {
                    char[] chars = q.toCharArray();

                    for (char c : chars) {
                        if (Character.isLetter(c) && !list.contains(c)) {
                            list.add(c);
                        }
                    }
                }

                double size = list.size();

                for (String q : quranList) {
                    char[] chars = q.toCharArray();

                    for (char c : chars) {
                        if (Character.isLetter(c)) {
                            quranRandomList.add((size - list.indexOf(c)) / size);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public double internalNextDouble(){
        return quranRandomList.get((int) (index.incrementAndGet() % quranRandomList.size()));
    }

    public static double nextDouble(){
        return INSTANCE.internalNextDouble();
    }
}
