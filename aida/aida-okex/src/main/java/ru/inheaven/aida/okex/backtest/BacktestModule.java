package ru.inheaven.aida.okex.backtest;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.guice.MyBatisModule;
import org.mybatis.guice.datasource.builtin.PooledDataSourceProvider;
import org.mybatis.guice.datasource.helper.JdbcHelper;
import ru.inheaven.aida.okex.mapper.StrategyMapper;
import ru.inheaven.aida.okex.mapper.TradeMapper;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * @author Anatoly A. Ivanov
 * 30.09.2017 19:55
 */
public class BacktestModule extends MyBatisModule {
    private static final Injector INJECTOR = Guice.createInjector(new BacktestModule());

    public static Injector getInjector() {
        return INJECTOR;
    }

    @Override
    protected void initialize() {
        install(JdbcHelper.MySQL);
        bindDataSourceProviderType(PooledDataSourceProvider.class);
        bindTransactionFactoryType(JdbcTransactionFactory.class);

        Properties properties = new Properties();
        properties.setProperty("mybatis.environment.id", "aida");
        properties.setProperty("mybatis.configuration.mapUnderscoreToCamelCase", "true");
        properties.setProperty("JDBC.host", "146.196.54.39");
        properties.setProperty("JDBC.port", "3306");
        properties.setProperty("JDBC.schema", "aida?useSSL=false");
        properties.setProperty("JDBC.username", "aida");
        properties.setProperty("JDBC.password", "aida");
        Names.bindProperties(binder(), properties);

        addMapperClasses();
    }

    private void addMapperClasses(){
        addMapperClass(TradeMapper.class);
        addMapperClass(StrategyMapper.class);
    }

    public static void main(String[] args) {
        //getInjector().getInstance(BacktestService.class);
        getInjector().getInstance(JsonMysqlTest.class);

        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
