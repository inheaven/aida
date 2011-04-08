package ru.inhell.aida.ssa;

import com.google.inject.Singleton;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.03.11 17:56
 */
@Singleton
public class VectorForecastSSAService {
    private List<VectorForecastSSA> vectorForecastSSAList = new ArrayList<VectorForecastSSA>();
    private IRemoteVSSA remoteVSSA;

    public VectorForecastSSA getVectorForecastSSA(int n, int l, int p, int m){
        for (VectorForecastSSA vssa : vectorForecastSSAList){
            if (n == vssa.getN() && l == vssa.getL() && p == vssa.getP() && m == vssa.getM()){
                return vssa;
            }
        }

        VectorForecastSSA vssa = new VectorForecastSSA(n, l, p, m);
        vectorForecastSSAList.add(vssa);

        return vssa;
    }

    public float[] execute(int n, int l, int p, int m, float[] timeSeries){
        return getVectorForecastSSA(n, l, p, m).execute(timeSeries);
    }

    public float[] executeRemote(int n, int l, int p, int m, float[] timeSeries)
            throws RemoteException, NotBoundException {
        if (remoteVSSA == null){
            Registry registry = LocateRegistry.getRegistry();
            remoteVSSA = (IRemoteVSSA) registry.lookup("RemoteVSSA");
        }

        return remoteVSSA.execute(n, l, p, m, timeSeries);
    }
}
