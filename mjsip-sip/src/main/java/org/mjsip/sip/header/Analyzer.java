/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package org.mjsip.sip.header;

/**
 * Helper for parsing strings.
 */
public final class Analyzer {

	private static final String WS = " \t\n\r";

	/**
	 * The source that is analyzed.
	 */
	private final String _src;

	/**
	 * The position of the end of the input.
	 */
	private int _length;

	/**
	 * The start position to extract contents from the input.
	 */
	private int _start = 0;

	/**
	 * The current position.
	 */
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
	 * Skips over white space.
	 */
	public Analyzer skipWSPCRLF() {
		return skipChars(WS);
	}

	/**
	 * Moves the position to the next white space character.
	 */
	public Analyzer findWS() {
		return findChars(WS);
	}

	/**
	 * Skips over all characters in the given string.
	 */
	public Analyzer skipChars(String chars) {
		return readChars(chars).drop();
	}

	/**
	 * Drops all content read so far.
	 */
	private Analyzer drop() {
		_start = _stop;
		return this;
	}

	/**
	 * Searches the next character that is contained in the given string.
	 * 
	 * <p>
	 * Upon return, the current position points to a character contained in the given string or is
	 * at the end of the input.
	 * </p>
	 */
	public Analyzer findChars(String chars) {
		while (!eof() && chars.indexOf(at()) < 0) {
			_stop++;
		}
		return this;
	}

	/**
	 * Consumes all characters that are contained in the given string.
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
	 * Advances the position until the given character is found in the input.
	 */
	public Analyzer findChar(char ch) {
		while (!eof() && !lookingAt(ch)) {
			_stop++;
		}
		return this;
	}

	/**
	 * Sets the length to the current position and reverts to the previous position.
	 */
	public Analyzer limit() {
		_length = _stop;
		_stop = _start;
		return this;
	}

	/**
	 * Whether the position is at the end of the input.
	 */
	public boolean eof() {
		return _stop >= _length;
	}

	/**
	 * The current character, or <code>-1</code> if the position is at the end of the input.
	 */
	public int currentChar() {
		return eof() ? -1 : at();
	}

	/**
	 * Returns the current character and advances the position to the next character.
	 */
	public int getChar() {
		int result = currentChar();
		skip();
		return result;
	}

	/**
	 * Whether no input is currently selected.
	 */
	public boolean empty() {
		return _stop == _start;
	}

	/**
	 * Whether some input has been selected.
	 */
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

	/**
	 * Whether the current character is the given one.
	 */
	public boolean lookingAt(char ch) {
		return at() == ch;
	}

	/**
	 * The rest of the input from the start position to the end of the input.
	 */
	public String remaining() {
		return _src.substring(_start, _length);
	}

}
