package ru.inhell.aida.oracle;

import com.google.inject.Inject;
import org.apache.ibatis.session.SqlSessionManager;
import ru.inhell.aida.entity.Quote;
import ru.inhell.aida.entity.VectorForecast;
import ru.inhell.aida.entity.VectorForecastData;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.03.11 19:22
 */
public class VectorForecastBean {
    @Inject
    private SqlSessionManager sm;

    private final static String NS = VectorForecastBean.class.getName();

    public void save(VectorForecast entity, List<Quote> quotes, float[] forecast){

    }

    public void save(VectorForecast entity){
        sm.insert(NS + ".insertVectorForecastEntity", entity);
    }

    public void save(List<VectorForecastData> data){
        sm.insert(NS + ".insertVectorForecastData", data);
    }

    @SuppressWarnings({"unchecked"})
    public List<VectorForecastData> getVectorForecastData(VectorForecast entity){
        return sm.selectList(NS + ".selectVectorForecastData", entity);
    }

    @SuppressWarnings({"unchecked"})
    public List<VectorForecast> getVectorForecastEntities(VectorForecast example){
        return sm.selectList(NS + ".selectVectorForecastEntities", example);
    }

    public Long getCount(VectorForecast entity){
        return (Long) sm.selectOne(NS + ".selectVectorForecastDataCount", entity);
    }

    public void update(VectorForecastData type){
        sm.update(NS + ".updateVectorForecastData", type);
    }
}
