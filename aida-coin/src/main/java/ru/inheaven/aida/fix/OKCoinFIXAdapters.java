package ru.inheaven.aida.fix;

import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.trade.Wallet;
import quickfix.FieldNotFound;
import quickfix.Message;
import ru.inheaven.aida.fix.fix44.AccountInfoResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Various adapters for converting from {@link Message} to XChange DTOs.
 */
public final class OKCoinFIXAdapters {

	private OKCoinFIXAdapters() {
	}

	public static AccountInfo adaptAccountInfo(AccountInfoResponse message)
			throws FieldNotFound {
		String[] currencies = message.getCurrency().getValue().split("/");
		String[] balances = message.getBalance().getValue().split("/");

		int walletCount = currencies.length;
		List<Wallet> wallets = new ArrayList<Wallet>(walletCount);

		for (int i = 0; i < walletCount; i++) {
			Wallet wallet = new Wallet(currencies[i],
					new BigDecimal(balances[i]));
			wallets.add(wallet);
		}

		return new AccountInfo(null, wallets);
	}

}
