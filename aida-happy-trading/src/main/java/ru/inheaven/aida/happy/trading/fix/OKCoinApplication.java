package ru.inheaven.aida.happy.trading.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.OrdType;
import quickfix.field.Side;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.MessageCracker;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.fix.fix44.AccountInfoResponse;

import java.util.Date;

public class OKCoinApplication extends MessageCracker implements Application {

	private final Logger log = LoggerFactory.getLogger(OKCoinApplication.class);



    private Account account;
	private SessionID sessionId;

	public OKCoinApplication(Account account) {




	}


	@Override
	public void onCreate(SessionID sessionId) {
        this.sessionId = sessionId;
	}

	@Override
	public void onLogon(SessionID sessionId) {
        this.sessionId = sessionId;
	}

	@Override
	public void onLogout(SessionID sessionId) {
	}






    protected void onTrade(Trade trade){
    }

    protected void onOrder(Order order){
    }

    protected void onDepth(Depth depth){
    }

	public void sendMessage(Message message) {
		log.trace("sending message: {}", message);
		Session.lookupSession(sessionId).send(message);
	}





}
