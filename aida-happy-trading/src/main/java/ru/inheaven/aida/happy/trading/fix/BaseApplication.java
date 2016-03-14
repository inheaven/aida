package ru.inheaven.aida.happy.trading.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.MsgType;
import quickfix.field.Password;
import quickfix.field.Username;
import ru.inheaven.aida.happy.trading.fix.fix44.AccountInfoResponse;


/**
 * inheaven on 14.03.2016.
 */
public class BaseApplication extends quickfix.fix44.MessageCracker implements Application{
    private Logger log = LoggerFactory.getLogger(BaseApplication.class);
    private DataDictionary dataDictionary;

    private String apiKey;
    private String secretKey;

    public BaseApplication(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;

        try {
            dataDictionary = new DataDictionary("FIX44.xml");
        } catch (ConfigError e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCreate(SessionID sessionId) {
        log.info("onCreate {}", sessionId);
    }

    @Override
    public void onLogon(SessionID sessionId) {
        log.info("onLogon {}", sessionId);
    }

    @Override
    public void onLogout(SessionID sessionId) {
        log.info("onLogout {}", sessionId);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        if (log.isTraceEnabled()) {
            log.trace("toAdmin: {}", message);
            log.trace("toAdmin: {}", message.toXML(dataDictionary));
        }

        try {
            String msgType = message.getHeader().getString(MsgType.FIELD);

            if (MsgType.LOGON.equals(msgType) || MsgType.HEARTBEAT.equals(msgType)) {
                message.setField(new Username(apiKey));
                message.setField(new Password(secretKey));
            }
        } catch (FieldNotFound e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        if (log.isTraceEnabled()) {
            log.trace("fromAdmin: {}", message);
            log.trace("fromAdmin: {}", message.toXML(dataDictionary));
        }
    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        if (log.isTraceEnabled()) {
            log.trace("toApp: {}", message);
            log.trace("toApp: {}", message.toXML(dataDictionary));
        }
    }

    @Override
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        if (log.isTraceEnabled()) {
            log.trace("fromApp: {}", message);
            log.trace("fromApp: {}", message.toXML(dataDictionary));
        }
        crack(message, sessionId);
    }

    @Override
    public void crack(quickfix.Message message, SessionID sessionId) throws UnsupportedMessageType, FieldNotFound,
            IncorrectTagValue {
        if (message instanceof AccountInfoResponse) {
            onMessage(message, sessionId);
        } else {
            if ("9".equals(message.getHeader().getString(MsgType.FIELD))){
                log.warn("crack -> {}", message.toString());

                return;
            }

            super.crack(message, sessionId);
        }
    }

    public void sendMessage(SessionID sessionId, Message message) {
        if (log.isTraceEnabled()) {
            log.trace("sendMessage: {}", message);
            log.trace("sendMessage: {}", message.toXML(dataDictionary));
        }

        Session.lookupSession(sessionId).send(message);
    }
}
