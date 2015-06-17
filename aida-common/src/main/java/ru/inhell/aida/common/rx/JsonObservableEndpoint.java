package ru.inhell.aida.common.rx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.PublishSubject;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.websocket.*;
import java.io.StringReader;

/**
 * @author inheaven on 01.05.2015 3:36.
 */
public class JsonObservableEndpoint extends Endpoint{
    private Logger log = LoggerFactory.getLogger(getClass());

    private PublishSubject<String> subject;
    private Observable<JsonObject> jsonObservable;

    private Session session;

    public JsonObservableEndpoint() {
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;

        subject = PublishSubject.create();

        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                subject.onNext(message);
            }
        });

        jsonObservable = subject.flatMapIterable(s -> Json.createReader(new StringReader(s)).readArray())
                .filter(j -> j.getValueType().equals(JsonValue.ValueType.OBJECT))
                .map(j -> (JsonObject)j);
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        subject.onCompleted();

        log.warn("json observable endpoint close {} ", closeReason);
    }

    @Override
    public void onError(Session session, Throwable thr) {
        subject.onError(thr);

        log.error("json observable endpoint error", thr);
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
