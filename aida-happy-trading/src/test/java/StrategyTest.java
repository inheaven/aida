import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import java.math.BigDecimal;

/**
 * @author inheaven on 20.06.2015 0:33.
 */

public class StrategyTest {


    public static void main(String... args){
        test3();
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

}
