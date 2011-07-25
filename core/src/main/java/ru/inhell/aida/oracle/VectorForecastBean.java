package ru.inhell.aida.oracle;

import com.google.inject.Inject;
import org.apache.ibatis.session.SqlSessionManager;
import ru.inhell.aida.entity.*;
import ru.inhell.aida.util.DateUtil;
import ru.inhell.aida.util.VectorForecastUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static ru.inhell.aida.entity.ExtremumType.*;
import static ru.inhell.aida.entity.ExtremumType.MIN20;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.03.11 19:22
 */
public class VectorForecastBean {
    @Inject
    private SqlSessionManager sm;

    private final static String NS = VectorForecastBean.class.getName();

    public void save(VectorForecast vectorForecast, List<Quote> quotes, float[] forecast){
        Date date = quotes.get(vectorForecast.getN() - 1).getDate();

        //проверяем если предсказание уже сохранено на текущую дату
        if (!isVectorForecastDataExists(vectorForecast.getId(), date)) {
            List<VectorForecastData> dataList = new ArrayList<VectorForecastData>();

            for (int index = -vectorForecast.getM(); index <= vectorForecast.getM(); ++index){
                float price = forecast[vectorForecast.getN() + index];
                dataList.add(new VectorForecastData(vectorForecast.getId(), date, index, date, price));
            }

            extremum(dataList);

            try {
                save(dataList);
            } catch (Exception e) {
                //
            }
        }
    }

    private void extremum(List<VectorForecastData> dataList){
        for (int index = 5; index <= dataList.size() - 5; ++index) {
            VectorForecastData d = dataList.get(index);

            if (VectorForecastUtil.isMax(dataList, index, 5)){
                d.setType(MAX5);

                if (VectorForecastUtil.isMax(dataList, index, 10)){
                    d.setType(MAX10);

                    if (VectorForecastUtil.isMax(dataList, index, 15)){
                        d.setType(MAX15);

                        if (VectorForecastUtil.isMax(dataList, index, 20)){
                            d.setType(MAX20);

                            if (VectorForecastUtil.isMax(dataList, index, 30)){
                                d.setType(MAX30);
                            }
                        }
                    }
                }
            }else if (VectorForecastUtil.isMin(dataList, index, 5)){
                d.setType(MIN5);

                if (VectorForecastUtil.isMin(dataList, index, 10)){
                    d.setType(MIN10);

                    if (VectorForecastUtil.isMin(dataList, index, 15)){
                        d.setType(MIN15);

                        if (VectorForecastUtil.isMin(dataList, index, 20)){
                            d.setType(MIN20);

                            if (VectorForecastUtil.isMin(dataList, index, 30)){
                                d.setType(MIN30);
                            }
                        }
                    }
                }
            }
        }
    }

    public void save(VectorForecast entity){
        sm.insert(NS + ".insertVectorForecast", entity);
    }

    public void save(List<VectorForecastData> dataList){
        sm.insert(NS + ".insertVectorForecastData", dataList);
    }

    @SuppressWarnings({"unchecked"})
    public List<VectorForecastData> getVectorForecastData(VectorForecastFilter filter){
        return sm.selectList(NS + ".selectVectorForecastData", filter);
    }

    @SuppressWarnings({"unchecked"})
    public List<VectorForecast> getVectorForecasts(VectorForecastFilter filter){
        return sm.selectList(NS + ".selectVectorForecasts", filter);
    }

    public boolean isVectorForecastDataExists(final Long vectorForecastId, final Date date){
        return (Boolean)sm.selectOne(NS + ".selectIsVectorForecastDataExists", new HashMap<String, Object>(){{
            put("vectorForecastId", vectorForecastId);
            put("date", date);
        }});
    }

    public boolean hasVectorForecastDataExtremum(final Long vectorForecastId, final Date date){
        return (Boolean)sm.selectOne(NS + ".selectHasVectorForecastDataExtremum", new HashMap<String, Object>(){{
            put("vectorForecastId", vectorForecastId);
            put("date", date);
        }});
    }

    public void update(VectorForecastData type){
        sm.update(NS + ".updateVectorForecastData", type);
    }

    public VectorForecast getOrCreateVectorForecast(String symbol, Interval interval,
                                                    int n, int l, int p, int m){
        VectorForecast vectorForecast = (VectorForecast) sm.selectOne(NS + ".selectVectorForecast",
                new VectorForecastFilter(symbol, interval, n, l, p, m));

        if (vectorForecast == null){
            vectorForecast = new VectorForecast(symbol, interval, n, l, p, m, DateUtil.now());
            save(vectorForecast);
        }

        return vectorForecast;
    }

    public Date getLastVectorForecastDataDate(Long vectorForecastId){
        return (Date) sm.selectOne(NS + ".selectLastVectorForecastDataDate", vectorForecastId);
    }
}
