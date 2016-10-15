package ru.inheaven.aida.happy.trading.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author inheaven on 16.10.2016.
 */
public class TorahRandom {
    private static TorahRandom INSTANCE = new TorahRandom();

    private List<Character> hebrewOrder = new ArrayList<>();
    private List<Character> torahList = new ArrayList<>();

    private AtomicLong index = new AtomicLong(0);

    public TorahRandom() {
        try {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(FileSystems.getDefault().getPath("/opt/data/torah"))) {
                stream.forEach(p -> {
                    try {
                        List<String> l = Files.readAllLines(p);

                        l.forEach(s -> {
                            char[] cs = s.toCharArray();

                            for (char c : cs){
                                if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HEBREW && Character.isLetter(c)){
                                    torahList.add(c);
                                }
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                hebrewOrder.addAll(new HashSet<>(torahList));

                hebrewOrder.sort(Comparator.reverseOrder());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double nextDouble(){
        Character c = INSTANCE.torahList.get((int) (INSTANCE.index.incrementAndGet() % INSTANCE.torahList.size()));

        return (double) (INSTANCE.hebrewOrder.indexOf(c) + 1)/INSTANCE.hebrewOrder.size();
    }
}
