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

    private MessageHandler messageHandler;

    private Session session;

    public JsonObservableEndpoint() {
        subject = PublishSubject.create();

        subject.doOnError(e -> log.error("error JsonObservableEndpoint", e));

        messageHandler = new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                subject.onNext(message);
            }
        };

        jsonObservable = subject
                .flatMapIterable(s -> {
                    try {
                        JsonStructure j = Json.createReader(new StringReader(s)).read();

                        return j.getValueType().equals(JsonValue.ValueType.ARRAY)
                                ? (JsonArray) j
                                : Collections.singletonList(j);
                    } catch (Exception e) {
                        log.error("error parse -> {}", s);
                        return Collections.emptyList();
                    }
                })
                .filter(j -> j != null && j.getValueType().equals(JsonValue.ValueType.OBJECT))
                .map(j -> (JsonObject)j);
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;

        session.addMessageHandler(messageHandler);

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
