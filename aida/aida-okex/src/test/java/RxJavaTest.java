import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * @author Anatoly A. Ivanov
 * 18.08.2017 10:29
 */
public class RxJavaTest {
    public static void main(String[] args) {
        FlowableProcessor<Integer> processor = PublishProcessor.create();

        processor.onBackpressureBuffer()
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .subscribe(i->{
                    System.out.println(Thread.currentThread().getName() + " out " +i);
                    Thread.sleep(1000);
                }, System.err::println);


        Executors.newSingleThreadScheduledExecutor().execute(() -> IntStream.range(0, 1000).forEach(i ->{
            System.out.println(Thread.currentThread().getName() + " in " + i);
            processor.onNext(i);
        }));
        Executors.newSingleThreadScheduledExecutor().execute(() -> IntStream.range(-1000, 0).forEach(i ->{
            System.out.println(Thread.currentThread().getName() + " in " + i);
            processor.onNext(i);
        }));


        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
