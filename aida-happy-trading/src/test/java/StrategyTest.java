import org.testng.annotations.Test;
import ru.inheaven.aida.happy.trading.util.QuranRandom;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

/**
 * @author inheaven on 20.06.2015 0:33.
 */

public class StrategyTest {


    public static void main(String... args) throws IOException {
        System.out.println(new Date());
        System.out.println();
    }

    private static void test1(){
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

    private static void test2(){
        BigDecimal decimal = BigDecimal.valueOf(4.0);
        for (int i=0; i<1000; ++i){
            decimal = decimal.add(BigDecimal.valueOf(0.02));

            System.out.println(decimal.divideToIntegralValue(BigDecimal.valueOf(0.02)));
        }
    }

    private static void test3(){
        PublishSubject<Integer> subject = PublishSubject.create();

        ConnectableObservable<Integer> observable = subject
                .map(i -> {
                    System.out.println(Thread.currentThread().getName() + " map " + i);

                    return i * i;
                })
                .observeOn(Schedulers.io()).publish();



        observable.subscribe(i -> {
            System.out.println(Thread.currentThread().getName() + " subscribe1 " + i);
        });

        observable.connect();

        observable.subscribe(i -> {
            System.out.println(Thread.currentThread().getName() + " subscribe2 " + i);
        });

        for (int i=2; i < 100; i += 2 ){
            subject.onNext(i);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void test4(){
        double price = 4.2;

        for (double spread = 0; spread < 0.05; spread += 0.001){
            System.out.println(spread + "," + (spread - (price + spread)*0.002));
        }
    }


    private static void testDivide(){
        System.out.println(2 % 3);
    }

    private static void testQuran() throws IOException {
//        Path path = FileSystems.getDefault().getPath("c:\\opt\\data", "quran-utf8.txt");
//        List<String> quranList = Files.readAllLines(path, Charset.forName("UTF-8"));
//
//        int max = Integer.MIN_VALUE;
//        int min = Integer.MAX_VALUE;
//
//        long count = 0;
//
//        for (String q : quranList){
//            char[] chars = q.toCharArray();
//
//            for (char c : chars){
//                if (Character.getDirectionality(c) == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC){
//                    if(c > max){
//                        max = c;
//                    }
//
//                    if (c < min){
//                        min = c;
//                    }
//                }
//
//
//            }
//        }
//
//        //min: 1569, max: 1610
//
//
//        System.out.println("min: " + (int)min + ", max: " + (int)max);

        for (int i = 0; i < 1000000; ++i){
            double q = QuranRandom.nextDouble();
            if (q >= 1){
                System.out.println(q);
            }
        }
    }

}
