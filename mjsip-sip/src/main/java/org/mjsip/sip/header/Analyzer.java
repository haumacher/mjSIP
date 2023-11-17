/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.sip.header;

/**
 * TODO
 *
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public final class Analyzer {

	private static final String WS = " \t\n\r";

	private final String _src;

	private final int _length;

	private int _start = 0;

	private int _stop = 0;

	/**
	 * Creates a {@link Analyzer}.
	 * 
	 * @param src
	 *        The text to analyze.
	 */
	public Analyzer(String src) {
		_src = src;
		_length = src.length();
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public Analyzer skipWSPCRLF() {
		return skipChars(WS);
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public Analyzer findWS() {
		return findChars(WS);
	}

	/**
	 * TODO
	 *
	 * @param string
	 * @return
	 */
	public Analyzer skipChars(String chars) {
		return readChars(chars).drop();
	}

	/**
	 * TODO
	 * 
	 * @return
	 *
	 */
	private Analyzer drop() {
		_start = _stop;
		return this;
	}

	/**
	 * TODO
	 *
	 * @param chars
	 * @return
	 */
	public Analyzer findChars(String chars) {
		while (!eof() && chars.indexOf(at()) < 0) {
			_stop++;
		}
		return this;
	}

	/**
	 * TODO
	 *
	 * @param string
	 * @return
	 */
	private Analyzer readChars(String chars) {
		while (currentIn(chars)) {
			_stop++;
		}
		return this;
	}

	/**
	 * Whether the {@link #currentChar()} is one of the given chars.
	 */
	public boolean currentIn(String chars) {
		return !eof() && chars.indexOf(at()) >= 0;
	}

	/**
	 * TODO
	 *
	 * @param c
	 * @return
	 */
	public Analyzer findChar(char ch) {
		while (!eof() && at() != ch) {
			_stop++;
		}
		return this;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public boolean eof() {
		return _stop >= _length;
	}

	public int currentChar() {
		return eof() ? -1 : at();
	}

	public int getChar() {
		int result = currentChar();
		skip();
		return result;
	}

	public boolean empty() {
		return _stop == _start;
	}

	public boolean nonEmpty() {
		return !empty();
	}

	/**
	 * Skips the {@link #currentChar()}.
	 */
	public Analyzer skip() {
		if (!eof()) {
			_stop++;
			drop();
		}
		return this;
	}

	/**
	 * The char currently looking at.
	 */
	private char at() {
		return _src.charAt(_stop);
	}

	/**
	 * The string before the {@link #currentChar()}.
	 */
	public String stringBefore() {
		return _src.substring(_start, _stop);
	}

	/**
	 * The string including the {@link #currentChar()}.
	 */
	public String stringIncluding() {
		return eof() ? stringBefore() : _src.substring(_start, _stop + 1);
	}

	/**
	 * The integer value starting at the {@link #currentChar()}.
	 */
	public int getInt() {
		return getInt(0);
	}

	/**
	 * The integer value starting at the {@link #currentChar()}, or the given default value, if
	 * there is no integer value.
	 */
	public int getInt(int defaultValue) {
		return drop().readChars("0123456789").empty() ? defaultValue : Integer.parseInt(stringBefore());
	}

}
