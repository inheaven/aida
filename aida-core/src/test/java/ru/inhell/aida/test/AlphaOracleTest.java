package ru.inhell.aida.test;

import org.apache.ibatis.session.SqlSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.calculation.Calculation;
import org.ujmp.core.enums.FileFormat;
import ru.inhell.aida.entity.Interval;
import ru.inhell.aida.entity.VectorForecast;
import ru.inhell.aida.entity.VectorForecastData;
import ru.inhell.aida.entity.VectorForecastFilter;
import ru.inhell.aida.common.mybatis.SqlSessionFactory;
import ru.inhell.aida.ssa.VectorForecastSSA;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static ru.inhell.aida.entity.ExtremumType.*;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 05.12.10 17:21
 */
public class AlphaOracleTest {
    private static Logger log = LoggerFactory.getLogger(AlphaOracleTest.class);

    private final static String NS = AlphaOracleTest.class.getName();

    private static int BUFFER_SIZE;

    private SqlSessionManager sm;

     //@Test
    public void train1() throws IOException, ParseException {
        AlphaOracleTest alphaOracleTest = new AlphaOracleTest();
            for (int p = 13; p < 20; p++){
                alphaOracleTest.train(1024, 512, p, 16);
            }
    }

    //@Test
    public void train2() throws IOException, ParseException {
        AlphaOracleTest alphaOracleTest = new AlphaOracleTest();
            for (int p = 23; p < 30; p++){
                alphaOracleTest.train(1024, 512, p, 16);
            }
    }

    //@Test
    public void train3() throws IOException, ParseException {
        AlphaOracleTest alphaOracleTest = new AlphaOracleTest();
            for (int p = 31; p < 40; p++){
                alphaOracleTest.train(1024, 512, p, 16);
            }
    }

    public static void main(String... args) throws IOException, ParseException {
        AlphaOracleTest ao = new AlphaOracleTest();

        BUFFER_SIZE = 64000;

        ao.extremum(1000, 225);
        ao.extremum(1000, 250);
        ao.extremum(1000, 300);
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

        VectorForecastSSA vectorForecast = new VectorForecastSSA(N, L, P, M);

        final float[] ts = new float[N];
        final float[] f = new float[N+M+L-1];

        sm = SqlSessionFactory.getSessionManager();

        VectorForecast entity = new VectorForecast(contract, Interval.ONE_MINUTE, N, L, P, M, new Date());
        save(entity);

        sm.startManagedSession();

        List<VectorForecastData> list = new ArrayList<VectorForecastData>(256);

        int len = quotes.length - N - M;

        long time = System.currentTimeMillis();

        for (int i = 0; i < len; ++i){
            int now = i + N;

            System.arraycopy(quotes, i, ts, 0, N);

            vectorForecast.execute(ts, f);

            //store
            for (int index = -M; index < M; ++index){

                if (f[N + index] != Float.NaN ) {
                    list.add(new VectorForecastData(entity.getId(),
                            sdf.parse(importFromCsv.getAsString(now, 2) + " " + importFromCsv.getAsString(now, 3)),
                            index,
                            sdf.parse(importFromCsv.getAsString(now + index, 2) + " " + importFromCsv.getAsString(now + index, 3)),
                            f[N + index]));
                }else{
                    log.warn("NAN hit: " +  importFromCsv.getAsString(now, 2) + " " + importFromCsv.getAsString(now, 3));
                }
            }

            if (i % 256 == 0){
                save(list);

                try {
                    sm.commit();
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                list.clear();

                log.info(i + ": " + importFromCsv.getAsString(now, 2) + " - " + (System.currentTimeMillis() - time) + "ms");

                time = System.currentTimeMillis();
            }
        }

        log.info("complete");
        try {
            sm.commit();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        sm.close();
    }

    public void extremum(int n, int l){
        sm = SqlSessionFactory.getSessionManager();

        VectorForecastFilter filter = new VectorForecastFilter();
        filter.setN(n);
        filter.setL(l);

        List<VectorForecast> entities = getVectorForecasts(filter);

        for (VectorForecast entity : entities) {
            long count = getCount(entity);
            int m = entity.getM();

            int pages = (int) (count/BUFFER_SIZE);

            for (int i = 0; i < pages; ++i){
                VectorForecastFilter filter1 = new VectorForecastFilter();
                //todo add filter

//                entity.setFirst(i*BUFFER_SIZE);
//                entity.setSize(BUFFER_SIZE);

                List<VectorForecastData> data = getVectorForecastData(filter1);

                int len = data.size() - 20;

                for (int j = 20; j < len; ++j){
                    VectorForecastData d = data.get(j);

                    if (d.getIndex() != 0){
                        continue;
                    }

                    if (isMax(data, j, 5)){
                        d.setType(MAX5);

                        if (isMax(data, j, 10) && m >= 10){
                            d.setType(MAX10);

                            if (isMax(data, j, 15) && m >= 15){
                                d.setType(MAX15);

                                if (isMax(data, j, 20) && m >= 20){
                                    d.setType(MAX20);
                                }
                            }
                        }
                    }else if (isMin(data, j, 5)){
                        d.setType(MIN5);

                        if (isMin(data, j, 10) && m >= 10){
                            d.setType(MIN10);

                            if (isMin(data, j, 15) && m >= 15){
                                d.setType(MIN15);

                                if (isMin(data, j, 20) && m >= 20){
                                    d.setType(MIN20);
                                }
                            }
                        }
                    }

                    if (d.getType() != null) {
                        update(d);
                        log.info(d.toString());
                    }
                }
            }
        }
    }

    private boolean isMax(List<VectorForecastData> data, int index, int delta){
        for (int i = 0; i < delta; ++i){
            if (data.get(index + i).getPrice() <= data.get(index + i + 1).getPrice()) return false;
            if (data.get(index - i).getPrice() <= data.get(index - i - 1).getPrice()) return false;
        }

        return true;
    }

    private boolean isMin(List<VectorForecastData> data, int index, int delta){
        for (int i = 0; i < delta; ++i){
            if (data.get(index + i).getPrice() >= data.get(index + i + 1).getPrice()) return false;
            if (data.get(index - i).getPrice() >= data.get(index - i - 1).getPrice()) return false;
        }

        return true;
    }

    private void save(VectorForecast entity){
        sm.insert(NS + ".insertVectorForecastEntity", entity);
    }

    private void save(List<VectorForecastData> data){
        sm.insert(NS + ".insertVectorForecastData", data);
    }

    @SuppressWarnings({"unchecked"})
    private List<VectorForecastData> getVectorForecastData(VectorForecastFilter filter){
        return sm.selectList(NS + ".selectVectorForecastData", filter);
    }

    @SuppressWarnings({"unchecked"})
    private List<VectorForecast> getVectorForecasts(VectorForecastFilter filter){
        return sm.selectList(NS + ".selectVectorForecasts", filter);
    }

    private Long getCount(VectorForecast entity){
        return (Long) sm.selectOne(NS + ".selectVectorForecastDataCount", entity);
    }

    private void update(VectorForecastData vectorForecastData){
        sm.update(NS + ".updateVectorForecastData", vectorForecastData);
    }
}
