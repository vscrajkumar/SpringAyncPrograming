package com.flex.adapter.helper;

import java.util.Iterator;

public class AlphaIterator implements Iterator<String> {
	private int maxIndex;
	private int index;
	private char[] alphabet;

	public AlphaIterator() {
		this(Integer.MAX_VALUE);
	}

	public AlphaIterator(int maxIndex) {
		this(maxIndex, "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray());
	}

	public AlphaIterator(char[] alphabet) {
		this(Integer.MAX_VALUE, alphabet);
	}

	public AlphaIterator(int maxIndex, char[] alphabet) {
		this.maxIndex = maxIndex;
		this.alphabet = alphabet;
		this.index = 1;
	}

	@Override
	public boolean hasNext() {
		return this.index < this.maxIndex;
	}

	@Override
	public String next() {
		return StringUtils.indexToColumnItr(this.index++, this.alphabet);
	}

	@Override
	public void remove() {

	}
}