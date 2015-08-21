package ru.inheaven.aida.happy.trading.fix.field;

import quickfix.DecimalField;

import java.math.BigDecimal;

/**
 * Available Quote Currency Balance(CNY in China domestic site, USD in international site).
 */
public class FreeQtCcy extends DecimalField {

	private static final long serialVersionUID = 20150129L;

	public static final int FIELD = 8103;

	public FreeQtCcy() {
		super(FIELD);
	}

	public FreeQtCcy(BigDecimal data) {
		super(FIELD, data);
	}

	public FreeQtCcy(double data) {
		super(FIELD, new BigDecimal(data));
	}

}
