package ru.inhell.aida.common.rx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.PublishSubject;

import javax.json.*;
import javax.websocket.*;
import java.io.StringReader;
import java.util.Collections;

/**

 * @author inheaven on 01.05.2015 3:36.
 */
public class JsonObservableEndpoint extends Endpoint{
    private Logger log = LoggerFactory.getLogger(getClass());

    private PublishSubject<String> subject;
    private Observable<JsonObject> jsonObservable;

    private Session session;

    public JsonObservableEndpoint() {
        subject = PublishSubject.create();

        jsonObservable = subject
                .flatMapIterable(s -> {
                    JsonStructure j = Json.createReader(new StringReader(s)).read();

                    return j.getValueType().equals(JsonValue.ValueType.ARRAY)
                            ? (JsonArray) j
                            : Collections.singletonList(j);
                })
                .filter(j -> j.getValueType().equals(JsonValue.ValueType.OBJECT))
                .map(j -> (JsonObject)j);
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;

        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                subject.onNext(message);
            }
        });

        log.info("connection open -> {} {}", session.getRequestURI(), session.getId());
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        log.warn("json observable endpoint close -> {} ", closeReason);
    }

    @Override
    public void onError(Session session, Throwable thr) {
        log.error("json observable endpoint error ->", thr);

        subject.onError(thr);
    }

    public Session getSession() {
        return session;
    }

    public Observable<String> getObservable(){
        return subject;
    }

    public Observable<JsonObject> getJsonObservable(){
        return jsonObservable;
    }

}
