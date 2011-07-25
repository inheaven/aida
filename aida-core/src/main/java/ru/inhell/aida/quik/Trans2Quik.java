package ru.inhell.aida.quik;

import com.sun.jna.*;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.win32.StdCallLibrary;

import java.util.HashMap;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.03.11 11:57
 */
public interface Trans2Quik extends Library{
    Trans2Quik INSTANCE = (Trans2Quik) Native.loadLibrary("TRANS2QUIK", Trans2Quik.class,
            new HashMap<Object, Object>(){{
                put(Library.OPTION_FUNCTION_MAPPER, StdCallLibrary.FUNCTION_MAPPER);
                put(Library.OPTION_CALLING_CONVENTION, Function.C_CONVENTION);

            }});

    int TRANS2QUIK_SUCCESS = 0;
    int TRANS2QUIK_FAILED = 1;
    int TRANS2QUIK_QUIK_TERMINAL_NOT_FOUND = 2;
    int TRANS2QUIK_DLL_VERSION_NOT_SUPPORTED = 3;
    int TRANS2QUIK_ALREADY_CONNECTED_TO_QUIK = 4;
    int TRANS2QUIK_WRONG_SYNTAX = 5;
    int TRANS2QUIK_QUIK_NOT_CONNECTED = 6;
    int TRANS2QUIK_DLL_NOT_CONNECTED = 7;
    int TRANS2QUIK_QUIK_CONNECTED = 8;
    int TRANS2QUIK_QUIK_DISCONNECTED = 9;
    int TRANS2QUIK_DLL_CONNECTED = 10;
    int TRANS2QUIK_DLL_DISCONNECTED = 11;
    int TRANS2QUIK_MEMORY_ALLOCATION_ERROR = 12;
    int TRANS2QUIK_WRONG_CONNECTION_HANDLE = 13;
    int TRANS2QUIK_WRONG_INPUT_PARAMS = 14;

    /**
     * Функция используется для установления связи библиотеки Trans2QUIK.dll с Рабочим местом QUIK.
     *
     * @param lpcstrConnectionParamsString Полный путь к каталогу с исполняемым файлом INFO.EXE, с которым
     *        устанавливается соединение
     * @param pnExtendedErrorCode В случае возникновения ошибки может содержать расширенный код ошибки
     * @param lpstrErrorMessage В случае возникновения ошибки может получать сообщение о возникшей ошибке
     * @param dwErrorMessageSize Содержит длину строки, на которую ссылается указатель lpstrErrorMessage
     * @return Возвращаемое число может принимать следующие значения:
     *         TRANS2QUIK_SUCCESS – соединение установлено успешно,
     *         TRANS2QUIK_QUIK_TERMINAL_NOT_FOUND – в указанном каталоге либо отсутствует INFO.EXE, либо у него
     *            не запущен сервис обработки внешних подключений, в pnExtendedErrorCode в этом случае передается 0,
     *         TRANS2QUIK_DLL_VERSION_NOT_SUPPORTED – используемая версия Trans2QUIK.dll не поддерживается
     *             указанным INFO.EXE, в pnExtendedErrorCode в этом случае передается 0,
     *         TRANS2QUIK_DLL_ALREADY_CONNECTED – соединение уже установлено, в pnExtendedErrorCode в этом случае
     *             передается 0,
     *         TRANS2QUIK_FAILED – произошла ошибка при установлении соединения, в pnExtendedErrorCode в этом случае
     *             передается дополнительный код ошибки
     */
    NativeLong TRANS2QUIK_CONNECT(String lpcstrConnectionParamsString, LongByReference pnExtendedErrorCode,
                                  byte[] lpstrErrorMessage, int dwErrorMessageSize);

    /**
     * Функция используется для разрыва связи библиотеки Trans2QUIK.dll с терминалом QUIK.
     *
     * @param pnExtendedErrorCode В случае возникновения ошибки может содержать расширенный код ошибки
     * @param lpstrErrorMessage В случае возникновения ошибки может получать сообщение о возникшей ошибке
     * @param dwErrorMessageSize Содержит длину строки, на которую ссылается указатель lpstrErrorMessage
     * @return Возвращаемое число может принимать следующие значения:
     *         TRANS2QUIK_SUCCESS – соединение библиотеки Trans2QUIK.dll с Рабочим местом QUIK разорвано успешно,
     *         TRANS2QUIK_FAILED – произошла ошибка при разрыве соединения, в pnExtendedErrorCode в этом случае
     *             передается дополнительный код ошибки,
     *         TRANS2QUIK_DLL_NOT_CONNECTED – попытка разорвать соединение при не установленной связи. В этом случае
     *             в pnExtendedErrorCode может передаваться дополнительный код ошибки
     */
    NativeLong TRANS2QUIK_DISCONNECT(LongByReference pnExtendedErrorCode, byte[] lpstrErrorMessage, int dwErrorMessageSize);

    /**
     * Функция используется для проверки наличия соединения между терминалом QUIK и сервером
     *
     * @param pnExtendedErrorCode В случае возникновения ошибки может содержать расширенный код ошибки
     * @param lpstrErrorMessage В случае возникновения ошибки может получать сообщение о возникшей ошибке
     * @param dwErrorMessageSize Содержит длину строки, на которую ссылается указатель lpstrErrorMessage
     * @return Возвращаемое число может принимать следующие значения:
     *         TRANS2QUIK_QUIK_CONNECTED – соединение установлено,
     *         TRANS2QUIK_QUIK_NOT_CONNECTED – соединение не установлено,
     *         TRANS2QUIK_DLL_NOT_CONNECTED – не установлена связь библиотеки Trans2QUIK.dll с терминалом QUIK.
     *             В этом случае проверить наличие или отсутствие связи терминала QUIK с сервером невозможно
     */
    NativeLong TRANS2QUIK_IS_QUIK_CONNECTED(LongByReference pnExtendedErrorCode, byte[] lpstrErrorMessage, int dwErrorMessageSize);

    /**
     * Функция используется для проверки наличия соединения между библиотекой Trans2QUIK.dll и терминалом QUIK.
     *
     * @param pnExtendedErrorCode В случае возникновения ошибки может содержать расширенный код ошибки
     * @param lpstrErrorMessage В случае возникновения ошибки может получать сообщение о возникшей ошибке
     * @param dwErrorMessageSize Содержит длину строки, на которую ссылается указатель lpstrErrorMessage
     * @return Возвращаемое число может принимать следующие значения:
     *         TRANS2QUIK_DLL_CONNECTED – соединение библиотеки Trans2QUIK.dll с терминалом QUIK установлено,
     *         TRANS2QUIK_DLL_NOT_CONNECTED – не установлена связь библиотеки Trans2QUIK.dll с терминалом QUIK
     */
    NativeLong TRANS2QUIK_IS_DLL_CONNECTED(LongByReference pnExtendedErrorCode, String lpstrErrorMessage, Integer dwErrorMessageSize);

    /**
     * Синхронная отправка транзакции. При синхронной отправке возврат из функции происходит только после получения
     * результата выполнения транзакции, либо после разрыва связи терминала QUIK с сервером.
     *
     * @param lpstTransactionString Строка с описанием транзакции. Формат строки тот же самый, что и при отправке
     *        транзакций через файл
     * @param pnReplyCode Получает статус выполнения транзакции. Значения статусов те же самые, что и при подаче
     *        заявок через файл
     * @param pdwTransId Получает значение TransID транзакции, указанной пользователем
     * @param pdOrderNum  В случае успеха получает номер заявки в торговой системе
     * @param lpstrResultMessage В случае успеха содержит сообщение торговой системы
     * @param dwResultMessageSize Содержит длину строки, на которую ссылается указатель lpstrResultMessage
     * @param pnExtendedErrorCode В случае возникновения ошибки может содержать расширенный код ошибки
     * @param lpstErrorMessage  В случае возникновения ошибки может получать сообщение о возникшей ошибке
     * @param dwErrorMessageSize  Содержит длину строки, на которую ссылается указатель lpstrErrorMessage
     * @return Возвращаемое число может принимать следующие значения:
     *         TRANS2QUIK_SUCCESS – транзакция успешно отправлена на сервер,
     *         TRANS2QUIK_WRONG_SYNTAX – строка транзакции заполнена неверно,
     *         TRANS2QUIK_DLL_NOT_CONNECTED – отсутствует соединение между библиотекой Trans2QUIK.dll и терминалом QUIK,
     *         TRANS2QUIK_QUIK_NOT_CONNECTED – отсутствует соединение между терминалом QUIK и сервером,
     *         TRANS2QUIK_FAILED – в pnExtendedErrorCode в этом случае может передаваться дополнительный код ошибки
     */
    NativeLong TRANS2QUIK_SEND_SYNC_TRANSACTION(String lpstTransactionString, LongByReference pnReplyCode,
                                                IntByReference pdwTransId, DoubleByReference pdOrderNum,
                                                byte[] lpstrResultMessage, int dwResultMessageSize,
                                                LongByReference pnExtendedErrorCode,
                                                byte[] lpstErrorMessage, int dwErrorMessageSize);

    /**
     * Асинхронная передача транзакции. При отправке асинхронной транзакции возврат из функции происходит сразу же,
     * а результат выполнения транзакции сообщается через соответствующую функцию обратного вызова.
     *
     * @param lpstTransactionString Строка с описанием транзакции. Формат строки такой же, что и при отправке транзакций
     *        через файл
     * @param pnExtendedErrorCode В случае возникновения ошибки может содержать расширенный код ошибки
     * @param lpstErrorMessage В случае возникновения ошибки может получать сообщение о возникшей ошибке
     * @param dwErrorMessageSize Содержит длину строки, на которую ссылается указатель lpstrErrorMessage
     * @return Возвращаемое число может принимать следующие значения:
     *         TRANS2QUIK_SUCCESS – транзакция успешно отправлена на сервер,
     *         TRANS2QUIK_WRONG_SYNTAX – строка транзакции заполнена неверно,
     *         TRANS2QUIK_DLL_NOT_CONNECTED – отсутствует соединение между библиотекой Trans2QUIK.dll и терминалом QUIK,
     *         TRANS2QUIK_QUIK_NOT_CONNECTED – отсутствует соединение между терминалом QUIK и сервером,
     *         TRANS2QUIK_FAILED – транзакцию отправить не удалось. В этом случае в пере­менную pnExtendedErrorCode
     *             может передаваться дополнительный код ошибки
     */
    NativeLong TRANS2QUIK_SEND_ASYNC_TRANSACTION(String lpstTransactionString, LongByReference pnExtendedErrorCode,
                                                 String lpstErrorMessage, int dwErrorMessageSize);

    /**
     * Описание прототипа функции обратного вызова для контроля за состоянием соединения между библиотекой
     * Trans2QUIK.dll и используемым терминалом QUIK и между используемым терминалом QUIK и сервером.
     */
    public static interface TRANS2QUIK_CONNECTION_STATUS_CALLBACK extends Callback{
        /**
         * @param nConnectionEvent  Возвращаемое число может принимать следующие значения:
         *        TRANS2QUIK_QUIK_CONNECTED – соединение между терминалом QUIK и сервером установлено,
         *        TRANS2QUIK_QUIK_DISCONNECTED – соединение между терминалом QUIK и сервером разорвано,
         *        TRANS2QUIK_DLL_CONNECTED – соединение между DLL и используемым терминалом QUIK установлено,
         *        TRANS2QUIK_DLL_DISCONNECTED – соединение между DLL и используемым терминалом QUIK разорвано
         * @param nExtendedErrorCode В случае возникновения ошибки может содержать расширенный код ошибки
         * @param lpstrInfoMessage В случае возникновения ошибки может получать сообщение о возникшей ошибке
         */
        void invoke(LongByReference nConnectionEvent, LongByReference nExtendedErrorCode, String lpstrInfoMessage);
    }

    /**
     * Описание прототипа функции обратного вызова для обработки полученной информации о соединении.
     * @param pfConnectionStatusCallback  Указывается адрес функции, которая будет обрабатывать информацию о состоянии
     *        связи библиотеки Trans2QUIK.dll с терминалом QUIK или терминала QUIK с сервером
     * @param pnExtendedErrorCode В случае возникновения ошибки может содержать расширенный код ошибки
     * @param lpstrErrorMessage В случае возникновения ошибки может получать сообщение о возникшей ошибке
     * @param dwErrorMessageSize Содержит длину строки, на которую ссылается указатель lpstrErrorMessage
     * @return Возвращаемое число может принимать следующие значения:
     *         TRANS2QUIK_SUCCESS – функция обратного вызова установлена,
     *         TRANS2QUIK_FAILED – функцию обратного вызова установить не удалось. В этом случае в переменную
     *             pnExtendedErrorCode может передаваться дополнительный код ошибки
     */
    NativeLong TRANS2QUIK_SET_CONNECTION_STATUS_CALLBACK(TRANS2QUIK_CONNECTION_STATUS_CALLBACK pfConnectionStatusCallback,
                                                         LongByReference pnExtendedErrorCode, String lpstrErrorMessage,
                                                         int dwErrorMessageSize);

    /**
     * Описание прототипа функции обратного вызова для обработки полученной информации об отправленной транзакции.
     */
    public static interface TRANS2QUIK_TRANSACTION_REPLY_CALLBACK extends Callback{
        /**
         * @param nTransactionResult Возвращаемое число может принимать следующие значения:
         *        TRANS2QUIK_SUCCESS – транзакция передана успешно,
         *        TRANS2QUIK_DLL_NOT_CONNECTED – отсутствует соединение между библиотекой Trans2QUIK.dll и
         *            терминалом QUIK,
         *        TRANS2QUIK_QUIK_NOT_CONNECTED – отсутствует соединение между терминалом QUIK и сервером,
         *        TRANS2QUIK_FAILED – транзакцию передать не удалось. В этом случае в пере­менную pnExtendedErrorCode
         *            может передаваться дополнительный код ошибки
         * @param nTransactionExtendedErrorCode В случае возникновения проблемы при выходе из функции обратного вызова
         *        в переменную может быть помещен расширенный код ошибки
         * @param nTransactionReplyCode Значения статусов те же самые, что и при подаче заявок через файл
         * @param dwTransId Содержимое параметра TransId, который получила зарегистрированная транзакция
         * @param dOrderNum Номер заявки, присвоенный торговой системой в результате выполнения транзакции
         * @param lpstrTransactionReplyMessage Сообщение от торговой системы или сервера QUIK
         */
        void invoke(NativeLong nTransactionResult, NativeLong nTransactionExtendedErrorCode, NativeLong nTransactionReplyCode,
                    int dwTransId, double dOrderNum, String lpstrTransactionReplyMessage);
    }

    /**
     *
     * @param pfTransactionReplyCallback Указывается ссылка на функцию, которая будет обрабатывать информацию
     *        об отправленной транзакции
     * @param pnExtendedErrorCode В случае возникновения ошибки может содержать расширенный код ошибки
     * @param lpstrErrorMessage В случае возникновения ошибки может получать сообщение о возникшей ошибке
     * @param dwErrorMessageSize  Содержит длину строки, на которую ссылается указатель lpstrErrorMessage
     * @return Возвращаемое число может принимать следующие значения:
     *         TRANS2QUIK_SUCCESS – функция обратного вызова установлена,
     *         TRANS2QUIK_FAILED – функцию обратного вызова установить не удалось.
     */
    NativeLong TRANS2QUIK_SET_TRANSACTIONS_REPLY_CALLBACK(TRANS2QUIK_TRANSACTION_REPLY_CALLBACK pfTransactionReplyCallback,
                                                          NativeLong pnExtendedErrorCode, String lpstrErrorMessage,
                                                          int dwErrorMessageSize);

    /**
     * Функция обратного вызова для получения информации о параметрах заявки.
     */
    public static interface TRANS2QUIK_ORDER_STATUS_CALLBACK extends Callback{
        /**
         * @param nMode Признак того, идет ли начальное получение заявок или нет, возможные значения: «0» – новая
         *        заявка, «1» - идет начальное получение заявок, «2» – получена последняя заявка из начальной рассылки
         * @param dwTransID TransID транзакции, породившей заявку. Имеет значение «0», если заявка не была порождена
         *        транзакцией из файла, либо если TransID неизвестен
         * @param dNumber Номер заявки
         * @param lpstrClassCode Код класса
         * @param lpstrSecCode Код бумаги
         * @param dPrice Цена заявки
         * @param nBalance Неисполненный остаток заявки
         * @param dValue Объем заявки
         * @param nIsSell Направление заявки: «0», если «Покупка», иначе «Продажа»
         * @param nStatus Состояние исполнения заявки: Значение «1» соответствует состоянию «Активна», «2» - «Снята»,
         *        иначе «Исполнена»
         * @param nOrderDescriptor Дескриптор заявки, может использоваться для следующих специальных функций в теле
         *        функции обратного вызова:
         *        long	OrderQty (long nOrderDescriptor) – возвращает количество заявки;
         *        long	OrderDate (long nOrderDescriptor) – возвращает дату заявки;
         *        long	OrderTime (long nOrderDescriptor) – возвращает время заявки;
         *        long	OrderActivationTime (long nOrderDescriptor) – возвращает время активации заявки;
         *        long	OrderWithdrawTime (long nOrderDescriptor) – возвращает время снятия заявки;
         *        long	OrderExpiry (long nOrderDescriptor) – возвращает дату окончания срока действия заявки;
         *        double	OrderAccruedInt (long nOrderDescriptor) – возвращает накопленный купонный доход заявки;
         *        double	OrderYield (long nOrderDescriptor) – возвращает доходность заявки;
         *        LPSTR	OrderUserID (long nOrderDescriptor) – возвращает строковый идентификатор трейдера,
         *            от имени которого отправлена заявка;
         *        long	OrderUID (long nOrderDescriptor) – возвращает UserID пользователя, указанный в заявке;
         *        LPSTR	OrderAccount (long nOrderDescriptor) – возвращает торговый счет, указанный в заявке;
         *        LPSTR	OrderBrokerRef (long nOrderDescriptor) – возвращает комментарий заявки;
         *        LPSTR	OrderClientCode (long nOrderDescriptor) – возвращает код клиента, отправившего заявку;
         *        LPSTR	OrderFirmID (long nOrderDescriptor) – возвращает строковый идентификатор организации
         *            пользователя, отправившего заявку.
         */
        void invoke(NativeLong nMode, int dwTransID, double dNumber, String lpstrClassCode, String lpstrSecCode,
                    double dPrice, NativeLong nBalance, double dValue, NativeLong nIsSell, NativeLong nStatus,
                    NativeLong nOrderDescriptor);
    }

    /**
     * @param nOrderDescriptor Дескриптор заявки
     * @return Возвращает количество заявки
     */
    NativeLong OrderQty(NativeLong nOrderDescriptor);

    /**
     * @param nOrderDescriptor Дескриптор заявки
     * @return Возвращает дату заявки
     */
    NativeLong OrderDate(NativeLong nOrderDescriptor);

    /**
     * @param nOrderDescriptor Дескриптор заявки
     * @return Возвращает время заявки
     */
    NativeLong OrderTime(NativeLong nOrderDescriptor);

    /**
     * @param nOrderDescriptor Дескриптор заявки
     * @return Возвращает время активации заявки
     */
    NativeLong OrderActivationTime(NativeLong nOrderDescriptor);

    /**
     * @param nOrderDescriptor Дескриптор заявки
     * @return Возвращает время снятия заявки
     */
    NativeLong OrderWithdrawTime(NativeLong nOrderDescriptor);

    /**
     * @param nOrderDescriptor Дескриптор заявки
     * @return Возвращает дату окончания срока действия заявки
     */
    NativeLong OrderExpiry(NativeLong nOrderDescriptor);

    /**
     * @param nOrderDescriptor Дескриптор заявки
     * @return Возвращает накопленный купонный доход заявки
     */
    double OrderAccruedInt(NativeLong nOrderDescriptor);

    /**
     * @param nOrderDescriptor Дескриптор заявки
     * @return Возвращает доходность заявки
     */
    double OrderYield(NativeLong nOrderDescriptor);

    /**
     * @param nOrderDescriptor Дескриптор заявки
     * @return Возвращает строковый идентификатор трейдера, от имени которого отправлена заявка
     */
    String OrderUserID(NativeLong nOrderDescriptor);

    /**
     * @param nOrderDescriptor Дескриптор заявки
     * @return Возвращает UserID пользователя, указанный в заявке
     */
    NativeLong OrderUID(NativeLong nOrderDescriptor);

    /**
     * @param nOrderDescriptor Дескриптор заявки
     * @return Возвращает торговый счет, указанный в заявке
     */
    String OrderAccount(NativeLong nOrderDescriptor);

    /**
     * @param nOrderDescriptor Дескриптор заявки
     * @return Возвращает комментарий заявки
     */
    String OrderBrokerRef(NativeLong nOrderDescriptor);

    /**
     * @param nOrderDescriptor Дескриптор заявки
     * @return Возвращает код клиента, отправившего заявку
     */
    String OrderClientCode(NativeLong nOrderDescriptor);

    /**
     * @param nOrderDescriptor Дескриптор заявки
     * @return Возвращает строковый идентификатор организации пользователя, отправившего заявку
     */
    String OrderFirmID(NativeLong nOrderDescriptor);

    /**
     * Функция обратного вызова для получения информации о сделке.
     */
    public static interface TRANS2QUIK_TRADE_STATUS_CALLBACK extends Callback{
        /**
         * @param nMode  Признак того, идет ли начальное получение сделок или нет, возможные значения: «0» – новая
         *        сделка, «1» - идет начальное получение сделок, «2» – получена последняя сделка из начальной рассылки;
         * @param dNumber Номер сделки
         * @param dOrderNum Номер заявки, породившей сделку
         * @param lpstrClassCode Код класса
         * @param lpstrSecCode Код бумаги
         * @param dPrice Цена сделки
         * @param nQty Количество сделки
         * @param dValue Направление сделки: «0», если «Покупка», иначе «Продажа»
         * @param nIsSell Объем сделки
         * @param nTradeDescriptor Дескриптор сделки, может использоваться для следующих специальных функций в функции
         *        обратного вызова:
         *        long	TradeDate (long nTradeDescriptor) – возвращает дату заключения сделки;
         *        long	TradeSettleDate (long nTradeDescriptor) – возвращает дату расчетов по сделке;
         *        long	TradeTime (long nTradeDescriptor) – возвращает время сделки;
         *        long	TradeIsMarginal (long nTradeDescriptor) – возвращает признак маржинальности сделки: «0», если
         *            «немаржинальная», иначе «маржинальная»;
         *        LPSTR	TradeCurrency (long nTradeDescriptor) – возвращает валюту в которой торгуется инструмент сделки;
         *        LPSTR	TradeSettleCurrency (long nTradeDescriptor) – возвращает валюту расчетов по сделке;
         *        LPSTR	TradeSettleCode (long nTradeDescriptor) – возвращает код расчетов по сделке;
         *        double	TradeAccruedInt (long nTradeDescriptor) – возвращает накопленный купонный доход сделки;
         *        double	TradeYield (long nTradeDescriptor) – возвращает доходность сделки;
         *        LPSTR	TradeUserID (long nTradeDescriptor) – возвращает строковый идентификатор трейдера, от имени
         *            которого заключена сделка;
         *        LPSTR	TradeAccount (long nTradeDescriptor) – возвращает торговый счет сделки;
         *        LPSTR	TradeBrokerRef (long nTradeDescriptor) – возвращает комментарий сделки;
         *        LPSTR	TradeClientCode (long nTradeDescriptor) – возвращает код клиента сделки;
         *        LPSTR	TradeFirmID (long nTradeDescriptor) – возвращает строковый идентификатор организации
         *            пользователя сделки;
         *        LPSTR	TradePartnerFirmID (long nTradeDescriptor) – возвращает строковый идентификатор
         *            организации-партнера по сделке;
         *        double	TradeTSCommission (long nTradeDescriptor) – возвращает величину суммарной комиссии по
         *            сделке;
         *        double	TradeClearingCenterCommission (long nTradeDescriptor) – возвращает величину комиссии
         *            за клиринг по сделке;
         *        double	TradeExchangeCommission (long nTradeDescriptor) – возвращает величину комиссии за торги
         *            по сделке;
         *        double	TradeTradingSystemCommission (long nTradeDescriptor) – возвращает величину комиссии
         *            за технический доступ по сделке;
         *        double	TradePrice2 (long nTradeDescriptor) – возвращает цену выкупа;
         *        double	TradeRepoRate (long nTradeDescriptor) – возвращает ставку РЕПО в процентах;
         *        double	TradeRepoValue (long nTradeDescriptor) – возвращает сумму РЕПО (сумма
         *            привлеченных/предоставленных по сделке РЕПО денежных средств);
         *        double	TradeRepo2Value (long nTradeDescriptor) – возвращает стоимость выкупа РЕПО;
         *        double	TradeAccruedInt2 (long nTradeDescriptor) – возвращает накопленный купонный доход при выкупе;
         *        long	TradeRepoTerm (long nTradeDescriptor) – возвращает срок РЕПО в календарных днях;
         *        double	TradeStartDiscount (long nTradeDescriptor) – возвращает начальный дисконт в процентах;
         *        double	TradeLowerDiscount (long nTradeDescriptor) – возвращает нижний предел дисконта в процентах;
         *        double	TradeUpperDiscount (long nTradeDescriptor) – возвращает верхний предел дисконта в процентах;
         *        LPSTR	TradeExchangeCode (long nTradeDescriptor) – возвращает строковый код биржи;
         *        LPSTR	TradeStationID (long nTradeDescriptor) – возвращает строковый идентификатор рабочей станции;
         *        long	TradeBlockSecurities (long nTradeDescriptor) – возвращает признак блокировки финансового
         *            инструмента на специальном счете на время операции РЕПО: «0» - еcли «не блокировать», иначе «блокировать»
         */
        void invoke(NativeLong nMode, double dNumber, double dOrderNum, String lpstrClassCode, String lpstrSecCode,
                    double dPrice, NativeLong nQty, double dValue, NativeLong nIsSell, NativeLong nTradeDescriptor);
    }

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает дату заключения сделки
     */
    NativeLong TradeDate(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает дату расчетов по сделке
     */
    NativeLong TradeSettleDate(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает время сделки
     */
    NativeLong TradeTime(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает признак маржинальности сделки: «0», если «немаржинальная», иначе «маржинальная»
     */
    NativeLong TradeIsMarginal(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает валюту в которой торгуется инструмент сделки
     */
    String TradeCurrency(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает валюту расчетов по сделке
     */
    String TradeSettleCurrency(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает код расчетов по сделке
     */
    String TradeSettleCode(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает накопленный купонный доход сделки
     */
    double TradeAccruedInt(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает доходность сделки
     */
    double TradeYield(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает строковый идентификатор трейдера, от имени которого заключена сделка
     */
    String TradeUserID(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает торговый счет сделки
     */
    String TradeAccount(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает комментарий сделки
     */
    String TradeBrokerRef(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает код клиента сделки
     */
    String TradeClientCode(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает строковый идентификатор организации пользователя сделки
     */
    String TradeFirmID(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает строковый идентификатор организации-партнера по сделке
     */
    String TradePartnerFirmID(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает величину суммарной комиссии по сделке
     */
    double TradeTSCommission(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает величину комиссии за клиринг по сделке
     */
    double TradeClearingCenterCommission(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает величину комиссии за торги по сделке
     */
    double TradeExchangeCommission(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает величину комиссии за технический доступ по сделке
     */
    double TradeTradingSystemCommission(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает цену выкупа
     */
    double TradePrice2(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает ставку РЕПО в процентах
     */
    double	TradeRepoRate(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает сумму РЕПО (сумма привлеченных/предоставленных по сделке РЕПО денежных средств)
     */
    double TradeRepoValue(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает стоимость выкупа РЕПО
     */
    double TradeRepo2Value(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает накопленный купонный доход при выкупе
     */
    double TradeAccruedInt2(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает срок РЕПО в календарных днях
     */
    long TradeRepoTerm(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает начальный дисконт в процентах
     */
    double TradeStartDiscount(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает нижний предел дисконта в процентах
     */
    double TradeLowerDiscount(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает верхний предел дисконта в процентах
     */
    double TradeUpperDiscount(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает строковый код биржи
     */
    String TradeExchangeCode(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает строковый идентификатор рабочей станции
     */
    String TradeStationID(NativeLong nTradeDescriptor);

    /**
     *
     * @param nTradeDescriptor Дескриптор сделки
     * @return возвращает признак блокировки финансового инструмента на специальном счете на время операции РЕПО:
     *         «0» - если «не блокировать», иначе «блокировать»
     */
    NativeLong TradeBlockSecurities(NativeLong nTradeDescriptor);

    /**
     * Функция служит для создания списка классов бумаг и инструментов для подписки на получение заявок по ним.
     *
     * @param lpstrClassCode Код класса, для которого будут заказаны заявки, если в качестве обоих входных параметров
     *        указаны пустые строки, то это означает, что заказано получение заявок по всем доступным инструментам
     * @param lpstrSeccodes Список кодов бумаг, разделенных символом «|», по которым будут заказаны заявки. Если в
     *        качестве значения указана пустая строка, то это означает, что заказано получение заявок по классу,
     *        указанному в параметре lpstrClassCode
     */
    void TRANS2QUIK_SUBSCRIBE_ORDERS(String lpstrClassCode, String lpstrSeccodes);

    /**
     * Функция служит для создания списка классов бумаг и инструментов для подписки на получение сделок по ним.
     *
     * @param lpstrClassCode Код класса, для которого будут заказаны сделки, если в качестве обоих входных параметров
     *        указаны пустые строки, то это означает, что заказано получение сделок по всем доступным инструментам
     * @param lpstrSeccodes Список кодов бумаг, разделенных символом «|», по которым будут заказаны сделки.
     *        Если в качестве значения указана пустая строка, то это означает, что заказано получение сделок по классу,
     *        указанному в параметре lpstrClassCode
     */
    void TRANS2QUIK_SUBSCRIBE_TRADES(String lpstrClassCode, String lpstrSeccodes);

    /**
     * Функция запускает процесс получения заявок по классам и инструментам, определенных функцией
     * TRANS2QUIK_SUBSCRIBE_ORDERS.
     *
     * @param pfnOrderStatusCallback Указатель на пользовательскую функцию обратного вызова для получения информации
     *        о заявках.
     */
    void TRANS2QUIK_START_ORDERS(TRANS2QUIK_ORDER_STATUS_CALLBACK pfnOrderStatusCallback);

    /**
     * Функция запускает процесс получения сделок с параметрами, установленными функцией TRANS2QUIK_SUBSCRIBE_TRADES.
     *
     * @param pfnTradesStatusCallback Указатель на пользовательскую функцию обратного вызова для получения информации
     *        о сделках.
     */
    void TRANS2QUIK_START_TRADES(TRANS2QUIK_TRADE_STATUS_CALLBACK pfnTradesStatusCallback);

    /**
     * Функция прерывает работу функции TRANS2QUIK_START_ORDERS и производит очистку списка получаемых инструментов,
     * сформированного функцией TRANS2QUIK_SUBSCRIBE_ORDERS.
     */
    void TRANS2QUIK_UNSUBSCRIBE_ORDERS();

    /**
     * Функция прерывает работу функции TRANS2QUIK_START_TRADES и производит очистку списка получаемых инструментов,
     * сформированного функцией TRANS2QUIK_SUBSCRIBE_TRADES.
     */
    void TRANS2QUIK_UNSUBSCRIBE_TRADES();
}
