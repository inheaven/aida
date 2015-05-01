package ru.inhell.aida.common.rx;

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
public class ObservableEndpoint extends Endpoint{
    private PublishSubject<String> subject = PublishSubject.create();

    private Session session;

    public ObservableEndpoint() {
    }

    public static ObservableEndpoint create(){
        return new ObservableEndpoint();
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
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        subject.onCompleted();
    }

    @Override
    public void onError(Session session, Throwable thr) {
        subject.onError(thr);
    }

    public Session getSession() {
        return session;
    }

    public Observable<String> getObservable(){
        return subject;
    }

    public Observable<JsonObject> getJsonObservable(){
        return subject.flatMapIterable(s -> Json.createReader(new StringReader(s)).readArray())
                .filter(j -> j.getValueType().equals(JsonValue.ValueType.OBJECT))
                .map(j -> (JsonObject)j);
    }

}
