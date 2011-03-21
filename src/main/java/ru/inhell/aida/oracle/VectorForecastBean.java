package ru.inhell.aida.oracle;

import com.google.inject.Inject;
import org.apache.ibatis.session.SqlSessionManager;
import ru.inhell.aida.entity.Quote;
import ru.inhell.aida.entity.VectorForecast;
import ru.inhell.aida.entity.VectorForecastData;
import ru.inhell.aida.entity.VectorForecastFilter;
import ru.inhell.aida.util.DateUtil;
import ru.inhell.aida.util.VectorForecastUtil;

import java.util.ArrayList;
import java.util.List;

import static ru.inhell.aida.entity.VectorForecastData.TYPE.*;
import static ru.inhell.aida.entity.VectorForecastData.TYPE.MIN20;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.03.11 19:22
 */
public class VectorForecastBean {
    @Inject
    private SqlSessionManager session;

    private final static String NS = VectorForecastBean.class.getName();

    public void save(VectorForecast vectorForecast, List<Quote> quotes, float[] forecast){
        List<VectorForecastData> dataList = new ArrayList<VectorForecastData>();

        for (int index = -vectorForecast.getM(); index < vectorForecast.getM(); ++index){
            //todo check +-1
            dataList.add(new VectorForecastData(vectorForecast.getId(), quotes.get(vectorForecast.getN() - 1)
                    .getDate(), index, forecast[vectorForecast.getN() + index]));
        }

        extremum(dataList);

        save(dataList);
    }

    private void extremum(List<VectorForecastData> dataList){
        for (int index = 5; index < dataList.size() - 5; ++index) {
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
        session.insert(NS + ".insertVectorForecast", entity);
    }

    public void save(List<VectorForecastData> dataList){
        session.insert(NS + ".insertVectorForecastData", dataList);
    }

    @SuppressWarnings({"unchecked"})
    public List<VectorForecastData> getVectorForecastData(VectorForecast entity){
        return session.selectList(NS + ".selectVectorForecastData", entity);
    }

    @SuppressWarnings({"unchecked"})
    public List<VectorForecast> getVectorForecast(VectorForecastFilter filter){
        return session.selectList(NS + ".selectVectorForecasts", filter);
    }

    public Long getCount(VectorForecast entity){
        return (Long) session.selectOne(NS + ".selectVectorForecastDataCount", entity);
    }

    public void update(VectorForecastData type){
        session.update(NS + ".updateVectorForecastData", type);
    }

    public VectorForecast getOrCreateVectorForecast(String symbol, VectorForecast.INTERVAL interval,
                                                    int n, int l, int p, int m){
        VectorForecast vectorForecast = (VectorForecast) session.selectOne(NS + ".selectVectorForecast",
                new VectorForecastFilter(symbol, interval, n, l, p, m));

        if (vectorForecast == null){
            vectorForecast = new VectorForecast(symbol, interval, n, l, p, m, DateUtil.now());
            save(vectorForecast);
        }

        return vectorForecast;
    }
}
