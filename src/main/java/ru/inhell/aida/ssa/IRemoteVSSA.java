package ru.inhell.aida.ssa;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 08.04.11 12:25
 */
public interface IRemoteVSSA extends Remote {
    float[] execute(int n, int l, int p, int m, float[] timeSeries) throws RemoteException;
}
