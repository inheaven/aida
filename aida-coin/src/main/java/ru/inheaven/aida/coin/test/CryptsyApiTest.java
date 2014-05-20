package ru.inheaven.aida.coin.test;

import ru.inheaven.aida.coin.util.SignatureUtil;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import java.io.IOException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 20.05.2014 7:03
 */
public class CryptsyApiTest {
    private final static String PUB = "6a4547fb4f03ed615ccb5450b0895b0b7193a72a";
    private final static String SECRET = "40a361db5d497ea3282b1aba2e5484b6f90bc1d25af8995942d2826501fbbd8f94a26abe9f1d582e";

    public static void main(String... args) throws IOException {
        String nonce = System.currentTimeMillis() + "000";

        Object o = ClientBuilder.newClient().target("https://api.cryptsy.com/api")
                .request()
                .header("Key", PUB)
                .header("Sign", SignatureUtil.getHmacSHA512("method=getinfo&nonce=" + nonce, SECRET))
                .post(Entity.form(new Form()
                        .param("method", "getinfo")
                        .param("nonce", nonce)))
                .readEntity(String.class);

        System.out.println(o);
    }
}
