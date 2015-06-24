/**
 * @author inheaven on 20.06.2015 0:33.
 */

public class StrategyTest {


    public static void main(String... args){
        int[] prices = new int[]{90, 100, 110, 100, 90, 80, 90, 100, 110, 120};

        int l = 0;
        int s = 0;

        for (int i = 1; i < prices.length - 1; i++){
            if (prices[i] > prices[i-1]){
                l++;
            }

            if (prices[i] < prices[i-1]){
                s++;
            }
        }

        System.out.println("l = " + l + ", s = " + s);
    }

}
