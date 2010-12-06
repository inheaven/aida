package ru.inhell.aida.oracle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.calculation.Calculation;
import org.ujmp.core.enums.FileFormat;
import ru.inhell.aida.entity.VectorForecastData;
import ru.inhell.aida.entity.VectorForecastEntity;
import ru.inhell.aida.mybatis.SqlSessionFactory;
import ru.inhell.aida.ssa.VectorForecast;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 05.12.10 17:21
 */
public class AlphaOracle {
    private static Logger log = LoggerFactory.getLogger(AlphaOracle.class);

    public static void main(String... args) throws IOException, ParseException {
        AlphaOracle alphaOracle = new AlphaOracle();

        alphaOracle.train(1000, 200, 10, 10);
    }

    public void train(int N, int L, int P, int M) throws IOException, ParseException {
        Matrix importFromCsv = MatrixFactory.importFromFile(FileFormat.CSV, "E:\\Java\\Projects-2010\\aida\\data\\GAZP_091202_101202.csv", ";");
        String contract = "GAZP";

        Calendar start = Calendar.getInstance();
        start.set(2009, 11, 2);

        Calendar end = Calendar.getInstance();
        end.set(2010, 11, 2);

        final float[] quotes = importFromCsv.selectColumns(Calculation.Ret.LINK, 4).transpose().toFloatArray()[0];

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss");

        VectorForecast vectorForecast = new VectorForecast(N, L, P, M);

        final float[] ts = new float[N];
        final float[] f = new float[N+M+L-1];

        SqlSessionFactory.getSessionManager().startManagedSession();

        VectorForecastEntity entity = new VectorForecastEntity(contract, "minute", start.getTime(), end.getTime(), N, L, P, M, new Date());
        save(entity);

        for (int i = 0; i < quotes.length - N - M; ++i){
            int now = i + N;

            System.arraycopy(quotes, i, ts, 0, N);

            vectorForecast.execute(ts, f);

            //store
            for (int index = -M; index < M; ++index){
                save(new VectorForecastData(entity.getId(),
                        sdf.parse(importFromCsv.getAsString(now, 2) + " " + importFromCsv.getAsString(now, 3)),
                        index,
                        sdf.parse(importFromCsv.getAsString(now + index, 2) + " " + importFromCsv.getAsString(now + index, 3)),
                        f[N + index]));
            }

            if (i % 250 == 0){
                SqlSessionFactory.getSessionManager().commit();
                log.info(i + ": " + importFromCsv.getAsString(now, 2));
            }
        }
    }

    private void save(VectorForecastEntity entity){
        SqlSessionFactory.getSessionManager().insert(AlphaOracle.class.getName() + ".insertVectorForecastEntity", entity);
    }

    private void save(VectorForecastData data){
        SqlSessionFactory.getSessionManager().insert(AlphaOracle.class.getName() + ".insertVectorForecastData", data);
    }
}
