package ru.inheaven.aida.okex.service;

import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * @author Anatoly A. Ivanov
 * 02.09.2017 16:54
 */
@Singleton
public class ApiService {
    @Inject
    public void init(){

    }

    public static void main(String[] args) {
        URI baseUri = UriBuilder.fromUri("http://localhost/").port(9998).build();
        ResourceConfig config = new ResourceConfig(ApiService.class);
        HttpServer server = JdkHttpServerFactory.createHttpServer(baseUri, config);
    }

}
