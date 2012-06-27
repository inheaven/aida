package ru.inhell.aida.mybatis;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSessionManager;
import org.osgi.framework.BundleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.common.service.IProcedure;
import ru.inhell.aida.common.util.OsgiUtil;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 31.05.12 17:32
 */
@Startup
@Singleton
public class SqlSessionFactoryService {
    private static final Logger log = LoggerFactory.getLogger(SqlSessionFactoryService.class);

    private SqlSessionManager sessionManager;

    public SqlSessionManager getSessionManager() {
        return sessionManager;
    }

    @PostConstruct
    private void init(){
        Reader reader = null;

        try {
            reader = Resources.getResourceAsReader("mybatis-config.xml");
            SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
            XMLConfigBuilder parser = new XMLConfigBuilder(reader);

            Configuration configuration = parser.parse();

            //Underscore
            configuration.setMapUnderscoreToCamelCase(true);

            sessionManager = SqlSessionManager.newInstance(builder.build(configuration));
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
            try {
                if (reader != null){
                    reader.close();
                }
            } catch (IOException e) {
                //nothing
            }
        }
    }

    @SuppressWarnings("EjbProhibitedPackageUsageInspection")
    public void addAnnotationMappers(BundleEvent event){
        OsgiUtil.scanAnnotation(event, XmlMapper.class, new IProcedure<Class<?>>() {
            @Override
            public void apply(Class<?> c) {
                addMappedClass(c);
            }
        });
    }

    private void addMappedClass(Class _class){
        try {
            Configuration configuration = sessionManager.getConfiguration();

            String resource = _class.getName().replace('.', '/') + ".xml";

            ErrorContext.instance().resource(resource);
            InputStream inputStream = Resources.getResourceAsStream(resource);
            XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource,
                    configuration.getSqlFragments());
            mapperParser.parse();

            log.info("Class {} is mapped.", _class.getName());
        } catch (IOException e) {
            log.error("Ресурс не найден", e);
        }
    }
}


