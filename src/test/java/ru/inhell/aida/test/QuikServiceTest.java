package ru.inhell.aida.test;

import org.testng.annotations.Test;
import ru.inhell.aida.quik.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 30.03.11 16:47
 */
public class QuikServiceTest {
    public static void main(String... args) throws QuikException, QuikTransactionException {
        new QuikServiceTest().testOrder();
    }

    @Test
    public void testOrder() throws QuikTransactionException, QuikException {
        QuikService quikService = new QuikService();

        quikService.connect("C:\\Anatoly\\QUIK_VTB24_DEMO\\");
        quikService.checkConnection();

        QuikTransaction qt = quikService.buyFutures(1, "LKOH", 2000, 3);

        System.out.println(qt);

        quikService.disconnect();
    }

}
