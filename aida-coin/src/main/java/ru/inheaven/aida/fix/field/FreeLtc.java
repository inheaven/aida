package ru.inheaven.aida.fix.field;

import quickfix.DecimalField;

import java.math.BigDecimal;

public class FreeLtc extends DecimalField {

	private static final long serialVersionUID = 20150129L;

	public static final int FIELD = 8102;

	public FreeLtc() {
		super(FIELD);
	}

	public FreeLtc(BigDecimal data) {
		super(FIELD, data);
	}

	public FreeLtc(double data) {
		super(FIELD, new BigDecimal(data));
	}

}
