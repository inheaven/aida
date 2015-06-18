import org.junit.Test;
import rx.Subscriber;
import rx.subjects.PublishSubject;

import java.util.stream.DoubleStream;

/**
 * @author inheaven on 18.04.2015 2:49.
 */
public class RxTest {

    @Test
    public void test(){
        PublishSubject<Double> publishSubject = PublishSubject.create();
        publishSubject.subscribe(System.out::println);

        publishSubject.subscribe(new Subscriber<Double>(){

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Double aDouble) {
                System.out.println(aDouble + " (*)");
            }
        });

        DoubleStream.iterate(0, n -> n + 0.5).limit(10).forEach(publishSubject::onNext);
    }

}
