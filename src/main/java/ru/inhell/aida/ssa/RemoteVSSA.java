package ru.inhell.aida.ssa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.inject.AidaInjector;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 08.04.11 12:30
 */
public class RemoteVSSA implements IRemoteVSSA{
    private final static Logger log = LoggerFactory.getLogger(RemoteVSSA.class);

    private static VectorForecastSSAService vectorForecastSSAService;

    @Override
    public float[] execute(int n, int l, int p, int m, float[] timeSeries) {
        VectorForecastSSA vectorForecastSSA = vectorForecastSSAService.getVectorForecastSSA(n, l, p, m);

        float[] forecast = new float[vectorForecastSSA.forecastSize()];

        vectorForecastSSA.execute(timeSeries, forecast);

        return forecast;
    }

    public static void main(String... args) throws RemoteException, AlreadyBoundException {
        Registry registry = LocateRegistry.getRegistry(51099);
        registry.bind("RemoteVSSA", UnicastRemoteObject.exportObject(new RemoteVSSA(), 0));

        vectorForecastSSAService = AidaInjector.getInstance(VectorForecastSSAService.class);

        log.info("Remote server started.");
    }
}
