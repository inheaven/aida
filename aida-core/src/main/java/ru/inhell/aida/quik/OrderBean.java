package ru.inhell.aida.quik;

import com.google.inject.Inject;
import org.apache.ibatis.session.SqlSessionManager;
import ru.inhell.aida.entity.Order;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 15.08.11 23:41
 */
public class OrderBean {
    public static final String NS = OrderBean.class.getName();

    @Inject
    private SqlSessionManager sm;

    @SuppressWarnings({"unchecked"})
    public List<Order> getOrders(String date){
        return sm.selectList("selectOrdersByDate", date);
    }
}
