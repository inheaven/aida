package ru.inheaven.aida.okex;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.guice.MyBatisModule;
import org.mybatis.guice.datasource.builtin.PooledDataSourceProvider;
import org.mybatis.guice.datasource.helper.JdbcHelper;
import ru.inheaven.aida.okex.mapper.*;
import ru.inheaven.aida.okex.service.MetricWsService;
import ru.inheaven.aida.okex.service.StorageWsService;
import ru.inheaven.aida.okex.service.StrategyService;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class Module extends MyBatisModule{
    private static final Injector INJECTOR = Guice.createInjector(new Module());

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
        properties.setProperty("JDBC.host", "localhost");
        properties.setProperty("JDBC.port", "3306");
        properties.setProperty("JDBC.schema", "aida?useSSL=false");
        properties.setProperty("JDBC.username", "aida");
        properties.setProperty("JDBC.password", "aida");
        Names.bindProperties(binder(), properties);

        addMapperClasses();
    }

    private void addMapperClasses(){
        addMapperClass(TradeMapper.class);
        addMapperClass(OrderMapper.class);
        addMapperClass(PositionMapper.class);
        addMapperClass(InfoMapper.class);
        addMapperClass(StrategyMapper.class);
    }

    public static void main(String[] args) {
        getInjector().getInstance(MetricWsService.class);
        getInjector().getInstance(StorageWsService.class);

        getInjector().getInstance(StrategyService.class);

        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
