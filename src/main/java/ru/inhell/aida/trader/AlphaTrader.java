package ru.inhell.aida.trader;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.enums.FileFormat;
import ru.inhell.aida.entity.Quote;
import ru.inhell.aida.entity.VectorForecastData;
import ru.inhell.aida.entity.VectorForecastEntity;
import ru.inhell.aida.mybatis.SqlSessionFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * User: Anatoly A. Ivanov java@inhell.ru
 * Date: 03.01.11 14:52
 */
public class AlphaTrader {
    private static Logger log = LoggerFactory.getLogger(AlphaTrader.class);

    private final static String NS = AlphaTrader.class.getName();

    private static int BUFFER_SIZE = 64000;

    private SqlSessionManager sm = SqlSessionFactory.getSessionManager();

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss");

    public static void main(String... args) throws IOException, ParseException {
         new AlphaTrader().importQuotes();
    }

    private void importQuotes() throws IOException, ParseException {
        Matrix importFromCsv = MatrixFactory.importFromFile(FileFormat.CSV, "F:\\data\\GAZP_091204_101204.txt", ",");

        sm.startManagedSession(ExecutorType.BATCH);

        for (int i=1; i < importFromCsv.getRowCount(); ++i){
            save(new Quote(sdf.parse(importFromCsv.getAsString(i, 2) + " " + importFromCsv.getAsString(i, 3)),
                    importFromCsv.getAsFloat(i, 4),
                    importFromCsv.getAsFloat(i, 5),
                    importFromCsv.getAsFloat(i, 6),
                    importFromCsv.getAsFloat(i, 7),
                    importFromCsv.getAsInt(i, 8)));

            if (i%100 == 0){
                log.info(100*i/importFromCsv.getRowCount() + "%");
                sm.commit();
            }
        }
    }

    private void trade(int n, int l){
        VectorForecastEntity example = new VectorForecastEntity();
        example.setN(n);
        example.setL(l);

        List<VectorForecastEntity> entities = getVectorForecastEntities(example);

        for (VectorForecastEntity entity : entities) {
            long count = getCount(entity);

            int pages = (int) (count/BUFFER_SIZE);

            for (int i = 0; i < pages; ++i){
                entity.setFirst(i*BUFFER_SIZE);
                entity.setSize(BUFFER_SIZE);

                List<VectorForecastData> data = getVectorForecastData(entity);

                for (VectorForecastData d : data){



                }
            }
        }


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

    private void save(Quote quote){
        sm.insert(NS + ".insertQuote", quote);
    }
}
