package ru.inhell.aida.test;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ujmp.core.Matrix;
import ru.inhell.aida.common.mybatis.SqlSessionFactory;
import ru.inhell.aida.entity.Quote;
import ru.inhell.aida.entity.VectorForecast;
import ru.inhell.aida.entity.VectorForecastData;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * User: Anatoly A. Ivanov java@inhell.ru
 * Date: 03.01.11 14:52
 */
public class AlphaTraderTest {
    private static Logger log = LoggerFactory.getLogger(AlphaTraderTest.class);

    private final static String NS = AlphaTraderTest.class.getName();

    private static int BUFFER_SIZE = 64000;

    private SqlSessionManager sm = SqlSessionFactory.getSessionManager();

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss");

    public static void main(String... args) throws IOException, ParseException {
         new AlphaTraderTest().importQuotes();
    }

    private void importQuotes() throws IOException, ParseException {
        Matrix importFromCsv = Matrix.Factory.linkTo().file("F:\\data\\GAZP_091204_101204.txt").asDenseCSV(';');

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

    @SuppressWarnings({"unchecked"})
    private List<VectorForecastData> getVectorForecastData(VectorForecast entity){
        return sm.selectList(NS + ".selectVectorForecastData", entity);
    }

    @SuppressWarnings({"unchecked"})
    private List<VectorForecast> getVectorForecastEntities(VectorForecast example){
        return sm.selectList(NS + ".selectVectorForecastEntities", example);
    }

    private Long getCount(VectorForecast entity){
        return (Long) sm.selectOne(NS + ".selectVectorForecastDataCount", entity);
    }

    private void save(Quote quote){
        sm.insert(NS + ".insertQuote", quote);
    }
}
