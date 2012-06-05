package ru.inhell.aida.common.service;

import java.io.Serializable;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 05.06.12 15:34
 */
public class ProcessCommand implements Serializable{
    private boolean cancel;
    private boolean done;

    public void start(){
        cancel =false;
        done = false;
    }

    public void cancel(){
        cancel = true;
    }

    public void done(){
        done = true;
    }

    public boolean isCancel() {
        return cancel;
    }

    public boolean isDone() {
        return done;
    }
}
