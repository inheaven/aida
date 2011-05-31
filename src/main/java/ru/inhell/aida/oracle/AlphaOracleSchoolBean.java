package ru.inhell.aida.oracle;

import com.google.inject.Inject;
import org.apache.ibatis.session.SqlSessionManager;
import ru.inhell.aida.entity.AlphaOracleSchool;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 28.05.11 15:48
 */
public class AlphaOracleSchoolBean {
    @Inject
    private SqlSessionManager sm;

    private final static String NS = AlphaOracleSchoolBean.class.getName();

    @SuppressWarnings({"unchecked"})
    public List<AlphaOracleSchool> getAlphaOracleSchools(){
        return sm.selectList(NS + ".selectAlphaOracleSchools");
    }
}
