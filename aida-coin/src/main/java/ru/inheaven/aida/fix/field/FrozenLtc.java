package ru.inheaven.aida.fix.field;

import quickfix.DecimalField;

import java.math.BigDecimal;

public class FrozenLtc extends DecimalField {

	private static final long serialVersionUID = 20150129L;

	public static final int FIELD = 8105;

	public FrozenLtc() {
		super(FIELD);
	}

	public FrozenLtc(BigDecimal data) {
		super(FIELD, data);
	}

	public FrozenLtc(double data) {
		super(FIELD, new BigDecimal(data));
	}

}
