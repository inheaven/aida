package ru.inhell.aida.matrix.service;

import ru.inhell.aida.common.service.IProcessListener;
import ru.inhell.aida.matrix.entity.Matrix;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 27.07.12 16:03
 */

@Singleton
public class MatrixTimerService {
    @EJB
    private MatrixBean matrixBean;

    @Resource
    private TimerService timerService;

    private List<IProcessListener<List<Matrix>>> listeners;

    private void startTimer(){

    }

    private void stopTimer(){

    }

    public void addListener(){

    }

}
