import org.testng.annotations.Test;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * @author inheaven on 22.11.2015 18:15.
 */
public class CurrencyTest {
    @Test
    public void split(){
        String symbol = "BTC/CNY";

        System.out.println(Arrays.toString(symbol.split("/")));
    }

    @Test
    public void gaussian(){
        SecureRandom secureRandom = new SecureRandom();

        IntStream.range(0, 100).forEach(i -> {
            System.out.println(secureRandom.nextGaussian());
        });

    }
}
