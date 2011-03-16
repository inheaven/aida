package ru.inhell.aida.oracle;

import com.google.inject.Inject;
import org.apache.ibatis.session.SqlSessionManager;
import ru.inhell.aida.entity.VFDType;
import ru.inhell.aida.entity.VectorForecastData;
import ru.inhell.aida.entity.VectorForecastEntity;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.03.11 19:22
 */
public class VectorForecastBean {
    @Inject
    private SqlSessionManager sm;

    private final static String NS = VectorForecastBean.class.getName();

    public boolean isMax(List<VectorForecastData> data, int index, int delta){
        for (int i = 0; i < delta; ++i){
            if (data.get(index + i).getClose() <= data.get(index + i + 1).getClose()) return false;
            if (data.get(index - i).getClose() <= data.get(index - i - 1).getClose()) return false;
        }

        return true;
    }

    public boolean isMin(List<VectorForecastData> data, int index, int delta){
        for (int i = 0; i < delta; ++i){
            if (data.get(index + i).getClose() >= data.get(index + i + 1).getClose()) return false;
            if (data.get(index - i).getClose() >= data.get(index - i - 1).getClose()) return false;
        }

        return true;
    }

    public void save(VectorForecastEntity entity){
        sm.insert(NS + ".insertVectorForecastEntity", entity);
    }

    public void save(List<VectorForecastData> data){
        sm.insert(NS + ".insertVectorForecastData", data);
    }

    @SuppressWarnings({"unchecked"})
    public List<VectorForecastData> getVectorForecastData(VectorForecastEntity entity){
        return sm.selectList(NS + ".selectVectorForecastData", entity);
    }

    @SuppressWarnings({"unchecked"})
    public List<VectorForecastEntity> getVectorForecastEntities(VectorForecastEntity example){
        return sm.selectList(NS + ".selectVectorForecastEntities", example);
    }

    public Long getCount(VectorForecastEntity entity){
        return (Long) sm.selectOne(NS + ".selectVectorForecastDataCount", entity);
    }

    public void update(VFDType type){
        sm.update(NS + ".updateVectorForecastData", type);
    }
}
