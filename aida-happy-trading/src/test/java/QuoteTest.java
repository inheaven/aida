import java.math.BigDecimal;

/**
 * @author inheaven on 26.09.2015 1:55.
 */
public class QuoteTest {
    public static void main(String... arg) {
        for (double p = 1500; p <= 1550; p += 5){
            for (double i = 0.1; i <= 1; i += 0.1){
                for (double j = 0.1; j <= 1; j += 0.1){
                    System.out.println(p + ", " + (p+i) + ", " + i + ", " + new BigDecimal(j).setScale(4, BigDecimal.ROUND_HALF_DOWN) + ", " +
                            new BigDecimal((p+i)*j).setScale(4, BigDecimal.ROUND_HALF_DOWN).subtract(BigDecimal.valueOf(p*j)).setScale(4, BigDecimal.ROUND_HALF_DOWN));
                }
            }
        }
    }
}
