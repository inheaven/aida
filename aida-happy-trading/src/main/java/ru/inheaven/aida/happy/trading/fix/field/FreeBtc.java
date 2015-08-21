package ru.inheaven.aida.happy.trading.fix.field;

import quickfix.DecimalField;

import java.math.BigDecimal;

public class FreeBtc extends DecimalField {

	private static final long serialVersionUID = 20150129L;

	public static final int FIELD = 8101;

	public FreeBtc() {
		super(FIELD);
	}

	public FreeBtc(BigDecimal data) {
		super(FIELD, data);
	}

	public FreeBtc(double data) {
		super(FIELD, new BigDecimal(data));
	}

}
