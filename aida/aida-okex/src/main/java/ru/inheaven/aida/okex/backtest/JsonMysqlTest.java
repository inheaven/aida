package ru.inheaven.aida.okex.backtest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.inheaven.aida.okex.mapper.StrategyMapper;
import ru.inheaven.aida.okex.model.Order;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.IntStream;

/**
 * @author Anatoly A. Ivanov
 * 24.11.2017 17:06
 */
public class JsonMysqlTest {
    @Inject
    private StrategyMapper strategyMapper;

    @Inject
    public void test() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        Deque<Order> list = new ConcurrentLinkedDeque<>();

        IntStream.range(0, 2).forEach(i ->{
            String s = i + "";
            list.add(new Order(s, s, i, BigDecimal.ZERO, s));
        });

        strategyMapper.updateCreatedOrders(1L, objectMapper.writeValueAsString(list));

        String json = strategyMapper.getCreatedOrders(1L);

        List<Order> loaded = objectMapper.readValue(json, new TypeReference<List<Order>>(){});

        System.out.println(loaded);

    }
}
