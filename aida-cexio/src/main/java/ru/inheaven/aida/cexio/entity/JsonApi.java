package ru.inheaven.aida.cexio.entity;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.SyncInvoker;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 09.01.14 22:44
 */
public class JsonApi<T> {
    private SyncInvoker syncInvoker;
    private T object;
    private long time = System.currentTimeMillis();
    private Class<T> _class;
    private long wait = 5000;

    public JsonApi(String target, Class<T> _class) {
        this._class = _class;
        syncInvoker = ClientBuilder.newClient().target(target).request();
    }

    public T get(){
        if (object == null || System.currentTimeMillis() - time > wait){
            object = syncInvoker.get(_class);
        }

        return object;
    }

    public long getWait() {
        return wait;
    }

    public void setWait(long wait) {
        this.wait = wait;
    }
}
