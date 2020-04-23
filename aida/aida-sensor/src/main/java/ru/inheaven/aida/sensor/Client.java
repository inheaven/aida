package ru.inheaven.aida.sensor;

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * @author Anatoly A. Ivanov
 * 27.05.2018 5:30
 */
public class Client {
    public static void main(String[] args) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream("D:\\Java\\AIDA\\aida-sensor\\keys\\server-test.jks"),
                "fbhffbhf".toCharArray());

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore,"fbhffbhf".toCharArray());

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());

        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(new Socket("192.168.0.100", 1234), "192.168.0.100", 1234, false);

        sslSocket.startHandshake();

        SSLSession sslSession = sslSocket.getSession();

        System.out.println("SSLSession :");
        System.out.println("\tProtocol : "+sslSession.getProtocol());
        System.out.println("\tCipher suite : "+sslSession.getCipherSuite());

        // Start handling application content
        InputStream inputStream = sslSocket.getInputStream();
        OutputStream outputStream = sslSocket.getOutputStream();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream));

        // Write data
        printWriter.println("Hello server");
        printWriter.println();
        printWriter.flush();
    }
}
