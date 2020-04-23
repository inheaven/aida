package ru.inheaven.aida.svet;

import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.server.Server;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

import static io.moquette.spi.impl.Utils.readBytesAndRewind;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Anatoly A. Ivanov
 * 24.05.2018 19:08
 */
public class Mqtt {
    public static void main(String[] args) throws IOException {
        Server server = new Server();

        server.startServer();

        server.addInterceptHandler(new AbstractInterceptHandler(){

            @Override
            public String getID() {
                return null;
            }

            @Override
            public void onPublish(InterceptPublishMessage msg) {
                ByteBuf payload = msg.getPayload();
                byte[] payloadContent = readBytesAndRewind(payload);

                System.out.println(new String(payloadContent, UTF_8));
            }
        });
    }
}
