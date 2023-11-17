/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.sip.header;

/**
 * A header with a plain string value.
 */
public class GenericHeader extends Header {

	/** The header string, without terminating CRLF */
	private String _value;

	/**
	 * Creates a {@link GenericHeader}.
	 */
	public GenericHeader(String name, String value) {
		super(name);
		this._value = value;
	}

	/**
	 * Creates a {@link GenericHeader}.
	 */
	public GenericHeader(LegacyHeader hd) {
		this(hd.getName(), hd.getValue());
	}

	@Override
	public String getValue() {
		return _value;
	}

	/** Sets value of Header */
	public void setValue(String hvalue) {
		_value = hvalue;
	}

}
