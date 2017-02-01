package ru.inhell.aida.common.util;

import org.apache.commons.lang3.text.WordUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * @author Anatoly A. Ivanov
 *         Date: 01.02.2017.
 */
public class Base64Util {
    public static void main(String[] args) throws IOException {
        decode();
    }

    private static void encode() throws IOException {
        byte[] data = Files.readAllBytes(Paths.get("D:\\Backup\\Aida\\aida-20170201.rar"));
        String code = Base64.getEncoder().encodeToString(data);
        code = WordUtils.wrap(code, 64, "\n", true);

        List<String> lines = Arrays.asList(code.split("\n"));

        Files.write(Paths.get("D:\\Backup\\Aida\\aida-20170201.txt"), lines);
    }

    private static void decode() throws IOException {
        List<String> list = Files.readAllLines(Paths.get("D:\\OneDrive\\Aida\\aida-20170201.txt"));

        StringBuffer code = new StringBuffer();

        list.forEach(s -> code.append(s.replace("\n", "")));

        byte[] data = Base64.getDecoder().decode(code.toString());

        Files.write(Paths.get("D:\\Backup\\Aida\\aida-20170201_decoded.rar"), data);
    }




}
