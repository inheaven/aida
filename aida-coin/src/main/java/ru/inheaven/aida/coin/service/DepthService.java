package ru.inheaven.aida.coin.service;

import ru.inheaven.aida.coin.entity.Depth;
import rx.Observer;
import rx.subjects.PublishSubject;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;

/**
 * @author inheaven on 01.05.2015 13:25.
 */
@Singleton
public class DepthService {
    @EJB
    private EntityBean entityBean;

    private PublishSubject<Depth> depthSubject = PublishSubject.create();

    @PostConstruct
    public void start(){
        depthSubject.subscribe(entityBean::save);
    }

    public Observer<Depth> getDepthObserver(){
        return depthSubject;
    }
}
