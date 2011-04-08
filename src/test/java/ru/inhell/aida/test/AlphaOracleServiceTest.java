package ru.inhell.aida.test;

import org.testng.annotations.Test;
import ru.inhell.aida.entity.AlphaOracle;
import ru.inhell.aida.inject.AidaInjector;
import ru.inhell.aida.oracle.AlphaOracleBean;
import ru.inhell.aida.oracle.AlphaOracleService;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 23.03.11 16:43
 */
public class AlphaOracleServiceTest {
    @Test
    public void predictAll(){
        AlphaOracleBean bean = AidaInjector.getInstance(AlphaOracleBean.class);

        AlphaOracle ao = bean.getAlphaOracles().get(0);

        AlphaOracleService service = AidaInjector.getInstance(AlphaOracleService.class);

//        service.predict(ao, 60);
    }


}

