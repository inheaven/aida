package ru.inhell.aida.oracle;

import com.sun.org.apache.bcel.internal.generic.FLOAD;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.calculation.Calculation;
import org.ujmp.core.enums.FileFormat;
import ru.inhell.aida.entity.VFDType;
import ru.inhell.aida.entity.VectorForecastData;
import ru.inhell.aida.entity.VectorForecastEntity;
import ru.inhell.aida.mybatis.SqlSessionFactory;
import ru.inhell.aida.ssa.VectorForecast;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 05.12.10 17:21
 */
public class AlphaOracle {
    private static Logger log = LoggerFactory.getLogger(AlphaOracle.class);

    private final static String NS = AlphaOracle.class.getName();

    private static int BUFFER_SIZE;

    private SqlSessionManager sm;

    public static void main(String... args) throws IOException, ParseException {
        AlphaOracle ao = new AlphaOracle();

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

        VectorForecast vectorForecast = new VectorForecast(N, L, P, M);

        final float[] ts = new float[N];
        final float[] f = new float[N+M+L-1];

        sm = SqlSessionFactory.getSessionManager();

        VectorForecastEntity entity = new VectorForecastEntity(contract, "minute", start.getTime(), end.getTime(), N, L, P, M, new Date());
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

        VectorForecastEntity example = new VectorForecastEntity();
        example.setN(n);
        example.setL(l);

        List<VectorForecastEntity> entities = getVectorForecastEntities(example);

        for (VectorForecastEntity entity : entities) {
            long count = getCount(entity);
            int m = entity.getM();

            int pages = (int) (count/BUFFER_SIZE);

            for (int i = 0; i < pages; ++i){
                entity.setFirst(i*BUFFER_SIZE);
                entity.setSize(BUFFER_SIZE);

                List<VectorForecastData> data = getVectorForecastData(entity);

                int len = data.size() - 20;

                for (int j = 20; j < len; ++j){
                    VectorForecastData d = data.get(j);

                    if (d.getIndex() != 0){
                        continue;
                    }

                    VFDType type = null;

                    if (isMax(data, j, 5)){
                        type = new VFDType(d.getId(), entity.getN(), entity.getL(), VFDType.TYPE.MAX5);

                        if (isMax(data, j, 10) && m >= 10){
                            type = new VFDType(d.getId(), entity.getN(), entity.getL(), VFDType.TYPE.MAX10);

                            if (isMax(data, j, 15) && m >= 15){
                                type = new VFDType(d.getId(), entity.getN(), entity.getL(), VFDType.TYPE.MAX15);

                                if (isMax(data, j, 20) && m >= 20){
                                    type = new VFDType(d.getId(), entity.getN(), entity.getL(), VFDType.TYPE.MAX20);
                                }
                            }
                        }
                    }

                    if (isMin(data, j, 5)){
                        type = new VFDType(d.getId(), entity.getN(), entity.getL(), VFDType.TYPE.MIN5);

                        if (isMin(data, j, 10) && m >= 10){
                            type = new VFDType(d.getId(), entity.getN(), entity.getL(), VFDType.TYPE.MIN10);

                            if (isMin(data, j, 15) && m >= 15){
                                type = new VFDType(d.getId(), entity.getN(), entity.getL(), VFDType.TYPE.MIN15);

                                if (isMin(data, j, 20) && m >= 20){
                                    type = new VFDType(d.getId(), entity.getN(), entity.getL(), VFDType.TYPE.MIN20);
                                }
                            }
                        }
                    }

                    if (type != null) {
                        update(type);
                        log.info(type + ", " + d);
                    }
                }
            }
        }
    }

    private boolean isMax(List<VectorForecastData> data, int index, int delta){
        for (int i = 0; i < delta; ++i){
            if (data.get(index + i).getClose() <= data.get(index + i + 1).getClose()) return false;
            if (data.get(index - i).getClose() <= data.get(index - i - 1).getClose()) return false;
        }

        return true;
    }

    private boolean isMin(List<VectorForecastData> data, int index, int delta){
        for (int i = 0; i < delta; ++i){
            if (data.get(index + i).getClose() >= data.get(index + i + 1).getClose()) return false;
            if (data.get(index - i).getClose() >= data.get(index - i - 1).getClose()) return false;
        }

        return true;
    }

    private void save(VectorForecastEntity entity){
        sm.insert(NS + ".insertVectorForecastEntity", entity);
    }

    private void save(List<VectorForecastData> data){
        sm.insert(NS + ".insertVectorForecastData", data);
    }

    @SuppressWarnings({"unchecked"})
    private List<VectorForecastData> getVectorForecastData(VectorForecastEntity entity){
        return sm.selectList(NS + ".selectVectorForecastData", entity);
    }

    @SuppressWarnings({"unchecked"})
    private List<VectorForecastEntity> getVectorForecastEntities(VectorForecastEntity example){
        return sm.selectList(NS + ".selectVectorForecastEntities", example);
    }

    private Long getCount(VectorForecastEntity entity){
        return (Long) sm.selectOne(NS + ".selectVectorForecastDataCount", entity);
    }

    private void update(VFDType type){
        sm.update(NS + ".updateVectorForecastData", type);
    }
}
