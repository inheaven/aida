package ru.inheaven.aida.fix.field;

import quickfix.DecimalField;

import java.math.BigDecimal;

public class FrozenBtc extends DecimalField {

	private static final long serialVersionUID = 20150129L;

	public static final int FIELD = 8104;

	public FrozenBtc() {
		super(FIELD);
	}

	public FrozenBtc(BigDecimal data) {
		super(FIELD, data);
	}

	public FrozenBtc(double data) {
		super(FIELD, new BigDecimal(data));
	}

}
